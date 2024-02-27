package io.github.junrdev.sage.activities.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.customview.widget.Openable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.junrdev.sage.R
import io.github.junrdev.sage.adapter.SelectedFileAdapter
import io.github.junrdev.sage.model.FileItem
import io.github.junrdev.sage.model.SelectedItem
import io.github.junrdev.sage.model.User
import io.github.junrdev.sage.util.Constants
import io.github.junrdev.sage.util.Constants.auth
import io.github.junrdev.sage.util.Constants.filesblob
import io.github.junrdev.sage.util.Constants.filesmetadata
import java.util.Date

private const val TAG = "UploadDocuments"

class UploadDocuments : AppCompatActivity() {

    private lateinit var adapter: SelectedFileAdapter
    private val selectedFiles: MutableList<SelectedItem> = mutableListOf()
    private val uploaded: MutableList<String> = mutableListOf()
    private lateinit var filesRecycler: RecyclerView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_documents)

        filesRecycler = findViewById(R.id.filesRecycler)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        filesRecycler.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        adapter = SelectedFileAdapter(applicationContext, selectedFiles)
        filesRecycler.adapter = adapter
    }

    fun openFileSelector(view: View) {
        if (checkFilePermissions()) {
            val filePicker = Intent(Intent.ACTION_OPEN_DOCUMENT)
            filePicker.addCategory(Intent.CATEGORY_OPENABLE)
            filePicker.type = "application/pdf"
            startActivityForResult(filePicker, Constants.FILE_PICK_CODE)
        } else {
            requirePermissions()
            openFileSelector(view)
        }
    }

    private fun requirePermissions() {
        val storagePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                Constants.FILE_PICK_CODE
            )
        }
    }


    private fun checkFilePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == -1) {

            if (requestCode == Constants.FILE_PICK_CODE) {


                data?.data?.let { uri ->

                    var fname = getFileName(uri)

                    Log.d(TAG, "onActivityResult: $fname")

                    val _file = SelectedItem(uri, fname)

                    if (selectedFiles.contains(_file))
                        Toast.makeText(
                            applicationContext,
                            "File already selected, choose another one.",
                            Toast.LENGTH_SHORT
                        ).show()
                    else {
                        selectedFiles.add(_file)
                        adapter.notifyItemInserted(selectedFiles.size - 1)
                    }
                }

            }

        }
    }

    private fun getFileName(uri: Uri): String {
        var fname: String? = null

        contentResolver.query(uri, null, null, null, null)
            ?.use {
                if (it.moveToFirst()!!)
                    fname = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        if (fname == null) {
            fname = uri.path
            val cut = fname?.lastIndexOf('/')
            if (cut != -1) {
                fname = fname?.substring(cut!! + 1)
            }
        }

        return fname!!
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.uploadmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${item.itemId}")
        if (item.itemId == R.id.upload) {

            //perform upload
            if (selectedFiles.isEmpty()) {
                Toast.makeText(applicationContext, "No documents selected.", Toast.LENGTH_SHORT)
                    .show()
            } else
                uploadDocuments()

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun uploadDocuments() {
        selectedFiles.forEachIndexed { index, file ->
            val task = filesblob
                .child(file.fname)

            task.putFile(file.uri).addOnCompleteListener { upload ->

                // upload file
                if (upload.isSuccessful && upload.isComplete) {
                    task.downloadUrl.addOnCompleteListener { dl ->

                        // get download url
                        if (dl.isSuccessful && dl.isComplete) {
                            Log.d(TAG, "uploadDocuments: ${dl.result}")

                            val id = filesmetadata.document().id
                            val up = FileItem(
                                fileId = id,
                                fileName = file.fname,
                                fileDownloadUrl = "${dl.result}",
                                fileType = "pdf",
                                uploadedBy = auth.uid!!,
                                uploadDate = "${Date(System.currentTimeMillis())}",
                                categories = listOf("pdf")
                            )

                            filesmetadata.document(id)
                                .set(up)
                                .addOnCompleteListener { save ->

                                    // save metadata to firestore
                                    if (save.isComplete && save.isSuccessful) {
                                        uploaded.add(up.fileId)
                                        selectedFiles.remove(file)
                                        adapter.notifyItemRemoved(index)
                                        Log.d(TAG, "uploadDocuments: saved doc $id")
                                    }
                                }
                                .addOnFailureListener {
                                    Log.d(
                                        TAG,
                                        "saveFileMetadata: failed to save $upload error ${it.localizedMessage}"
                                    )
                                }
                        }
                    }
                }
            }.addOnFailureListener {
                Log.d(TAG, "uploadDocuments: failed to upload $file error ${it.localizedMessage}")
            }
        }

//        runOnUiThread {
//            if (selectedFiles.isEmpty()) {
//                SaveUserDataInBackground().execute(uploaded)
//            }
//        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // check if list is empty and prompt use
        /*
        if (selectedFiles.isNotEmpty()) {

            val dialog = AlertDialog.Builder(applicationContext)

            dialog.setTitle("Are you sure you want to exit ?")
            dialog.setMessage("You have ${selectedFiles.size} documents awaiting upload.\nThese will be discarded if you continue.")
            dialog.setNegativeButton("Continue") { _dialog, _d ->
                super.onBackPressed()
            }

            dialog.setOnDismissListener {
                it.dismiss()
            }

            dialog.show()
        }
        */

    }


    /*
    class SaveUserDataInBackground : AsyncTask<MutableList<String>, Void, Boolean>() {

        var isDone = false

        override fun doInBackground(vararg params: MutableList<String>?): Boolean {


            val d = params[0]
            Log.d(TAG, "doInBackground: $d")

            Constants.usersmetadata.document(auth.uid!!)
                .addSnapshotListener { data, ex ->

                    if (data!!.exists()) {
                        data!!.toObject(User::class.java)
                            ?.uploads?.addAll(d!!)

                        isDone = true
                    }

                    if (ex != null) {
                        Log.d(TAG, "doInBackground: failed ${ex.localizedMessage}")
                    }
                }
            return isDone
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (isDone)
                Log.d(TAG, "onPostExecute: saved user data")
            else
                Log.d(TAG, "onPostExecute: failed to save data")
        }
    }
    */
}
