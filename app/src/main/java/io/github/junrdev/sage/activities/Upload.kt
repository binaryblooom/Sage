package io.github.junrdev.sage.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.github.junrdev.sage.R
import io.github.junrdev.sage.activities.fragments.UploadDocuments
import io.github.junrdev.sage.activities.fragments.UploadImages
import io.github.junrdev.sage.model.SelectedItem

class Upload : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
    }

    fun openUploadImages(view: View) {
        val uploadImages = Intent(this, UploadImages::class.java)
        uploadImages.putExtra("type", "gallery")
        startActivity(uploadImages)
    }

    fun openUploadFiles(view: View) {
        val uploadImages = Intent(this, UploadDocuments::class.java)
        uploadImages.putExtra("type", "gallery")
        startActivity(uploadImages)
    }

    fun openUploadCamera(view: View) {
        val openCamera = Intent(this, UploadImages::class.java)
        openCamera.putExtra("type", "camera")
        startActivity(openCamera)
    }
}