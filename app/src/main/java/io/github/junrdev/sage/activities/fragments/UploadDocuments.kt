package io.github.junrdev.sage.activities.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import io.github.junrdev.sage.R
import io.github.junrdev.sage.adapter.SelectedFileAdapter
import io.github.junrdev.sage.model.FileItem
import io.github.junrdev.sage.model.SelectedItem
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
    private lateinit var uploadingProgress: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_documents)
        filesRecycler = findViewById(R.id.filesRecycler)
        toolbar = findViewById(R.id.toolbar)
        uploadingProgress = findViewById(R.id.uploadingProgress)
        setSupportActionBar(toolbar)

        filesRecycler.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        adapter = SelectedFileAdapter(applicationContext, selectedFiles) { showBottomDialog(it) }
        filesRecycler.adapter = adapter
    }

    private fun showBottomDialog(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val cur = selectedFiles[position]
        Log.d(TAG, "onBindViewHolder: $cur")
        bottomSheetDialog.setContentView(R.layout.editselecteditemdetailsbottomsheet)

        val title = bottomSheetDialog.findViewById<TextView>(R.id.fileTitle)
        title?.text = selectedFiles[position].fname


        val bio = bottomSheetDialog.findViewById<CheckBox>(R.id.biology)
        val maths = bottomSheetDialog.findViewById<CheckBox>(R.id.maths)
        val ss = bottomSheetDialog.findViewById<CheckBox>(R.id.socialScience)
        val research = bottomSheetDialog.findViewById<CheckBox>(R.id.research)
        val literature = bottomSheetDialog.findViewById<CheckBox>(R.id.literature)
        val programming = bottomSheetDialog.findViewById<CheckBox>(R.id.programming)
        val save = bottomSheetDialog.findViewById<CardView>(R.id.saveItem)

        save?.setOnClickListener {
            if (bio?.isChecked!!)
                cur.categories.add("Biology")
            if (maths?.isChecked!!)
                cur.categories.add("Maths")
            if (ss?.isChecked!!)
                cur.categories.add("Social Science")
            if (research?.isChecked!!)
                cur.categories.add("Research")
            if (literature?.isChecked!!)
                cur.categories.add("Literature")
            if (programming?.isChecked!!)
                cur.categories.add("Programming")

            bottomSheetDialog.dismiss()

            bottomSheetDialog.show()
        }
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
                            Log.d(TAG, "uploadDocuments: url ${dl.result}")
                            Log.d(TAG, "uploadDocuments: name ${task.name}")

                            val id = filesmetadata.push().key!!

                            val fileUp = FileItem(
                                fileId = id,
                                fileName = file.fname,
                                storageName = task.name,
                                fileDownloadUrl = "${dl.result}",
                                fileType = "pdf",
                                uploadedBy = auth.uid!!,
                                uploadDate = "${Date(System.currentTimeMillis())}",
                                categories = listOf("pdf")
                            )

                            filesmetadata.child(id)
                                .setValue(fileUp)
                                .addOnCompleteListener { }
                                .addOnFailureListener { }
                        }
                    }
                }
            }.addOnFailureListener {
                Log.d(
                    TAG,
                    "uploadDocuments: failed to upload $file error ${it.localizedMessage}"
                )
            }
        }
    }

}
