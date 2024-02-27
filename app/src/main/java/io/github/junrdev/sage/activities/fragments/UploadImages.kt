package io.github.junrdev.sage.activities.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import io.github.junrdev.sage.R
import io.github.junrdev.sage.adapter.SelectedImagesRecyclerAdapter
import io.github.junrdev.sage.model.FileItem
import io.github.junrdev.sage.model.SelectedItem
import io.github.junrdev.sage.util.Constants
import io.github.junrdev.sage.util.Constants.filesblob
import io.github.junrdev.sage.util.Constants.filesmetadata

private const val TAG = "UploadImages"

class UploadImages : AppCompatActivity() {

    private lateinit var imagesrecycler: RecyclerView

    private var selectedImages = mutableListOf<SelectedItem>()
    private lateinit var adapter: SelectedImagesRecyclerAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var uploadingProgress: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_images)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        uploadingProgress = findViewById(R.id.uploadingProgress)

        imagesrecycler = findViewById(R.id.selectedImagesrecycler)
        imagesrecycler.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        adapter = SelectedImagesRecyclerAdapter(selectedImages, applicationContext) {
            showBottomDialog(it)
        }
        imagesrecycler.adapter = adapter

    }

    private fun showBottomDialog(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val cur = selectedImages[position]
        Log.d(TAG, "onBindViewHolder: $cur")
        bottomSheetDialog.setContentView(R.layout.editselecteditemdetailsbottomsheet)

        val title = bottomSheetDialog.findViewById<TextView>(R.id.fileTitle)
        title?.text = selectedImages[position].fname

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
        }

        bottomSheetDialog.show()
    }

    fun selectImage(view: View) {
        if (intent.hasExtra("type")) {

            if (intent.getStringExtra("type").equals("gallery"))
                ImagePicker.with(this)
                    .galleryOnly()
                    .crop()
                    .compress(3072)
                    .galleryMimeTypes(arrayOf("image/png", "image/jpg", "image/jpeg"))
                    .start()
            else
                openCamera(view)
        }
    }

    fun openCamera(view: View) {
        ImagePicker.with(this)
            .cameraOnly()
            .crop()
            .compress(3072)
            .start()
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        )
            return true
        else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                Constants.IMAGE_PICK_CODE
            )
        return checkPermissions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == -1) {

            if (requestCode == ImagePicker.REQUEST_CODE) {

                val content = data?.data!!
                val fname = getFileName(content)
                val _file = SelectedItem(uri = content, fname)

                if (selectedImages.contains(_file))
                    Toast.makeText(
                        applicationContext,
                        "Please select another image.",
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    selectedImages.add(_file)
                    adapter.notifyItemInserted(selectedImages.size - 1)
                }

                Log.d(TAG, "onActivityResult: $selectedImages")
            }


        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

//        if (selectedImages.isNotEmpty())
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.uploadmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.upload) {

            Log.d(TAG, "onOptionsItemSelected: selected")
            Log.d(TAG, "onOptionsItemSelected: selected ${item.itemId}")

            if (selectedImages.isEmpty()) {
                Toast.makeText(applicationContext, "No items selected.", Toast.LENGTH_SHORT).show()
            } else {


                selectedImages.forEachIndexed { index, image ->

                    val task = filesblob.child(image.fname)

                    task.putFile(image.uri)
                        .addOnCompleteListener { upload ->
                            if (upload.isComplete && upload.isSuccessful) {

                                Log.d(TAG, "onOptionsItemSelected: ${upload.result}")


                                task.downloadUrl.addOnCompleteListener { url ->


                                    if (url.isComplete && url.isSuccessful) {

                                        val id = filesmetadata.push().key!!
                                        val dlurl = "${url.result}"

                                        Log.d(TAG, "onOptionsItemSelected: $dlurl")
                                        filesmetadata.child(id)
                                            .setValue(
                                                FileItem(
                                                    fileId = id,
                                                    fileName = image.fname,
                                                    fileType = "image",
                                                    fileDownloadUrl = dlurl,
                                                    categories = listOf("images"),
                                                    filePreview = dlurl
                                                )
                                            )
                                            .addOnCompleteListener { save ->
                                                if (save.isComplete && save.isSuccessful) {
                                                    selectedImages.remove(image)
                                                    adapter.notifyItemRemoved(index)
                                                }
                                            }.addOnFailureListener {
                                                Log.d(
                                                    TAG,
                                                    "onOptionsItemSelected: ${it.localizedMessage}"
                                                )
                                            }
                                    }
                                }

                            }

                        }
                        .addOnFailureListener {
                            Log.d(TAG, "onOptionsItemSelected: ${it.localizedMessage}")
                        }

                }

            }

        }
        return super.onOptionsItemSelected(item)
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

}