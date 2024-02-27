package io.github.junrdev.sage.activities.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_images)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        imagesrecycler = findViewById(R.id.selectedImagesrecycler)
        imagesrecycler.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        adapter = SelectedImagesRecyclerAdapter(selectedImages, applicationContext)
        imagesrecycler.adapter = adapter
    }

    fun selectImage(view: View) {
        ImagePicker.with(this)
            .galleryOnly()
            .crop()
            .compress(3072)
            .galleryMimeTypes(arrayOf("image/png", "image/jpg", "image/jpeg"))
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
                val _file = SelectedItem(uri = content, "${content.lastPathSegment}")

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

                                        val id = filesmetadata.document().id
                                        val dlurl = "${url.result}"

                                        Log.d(TAG, "onOptionsItemSelected: $dlurl")
                                        filesmetadata.document(id)
                                            .set(
                                                FileItem(fileId = id, fileName = image.fname, fileType = "image", fileDownloadUrl = dlurl, categories = listOf("images"), filePreview = dlurl)
                                            )
                                            .addOnCompleteListener { save ->
                                                if (save.isComplete && save.isSuccessful) {
                                                    selectedImages.remove(image)
                                                    adapter.notifyItemRemoved(index)
                                                }
                                            }.addOnFailureListener {
                                                Log.d(TAG, "onOptionsItemSelected: ${it.localizedMessage}")
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
}