package io.github.junrdev.sage.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.github.junrdev.sage.model.FileItem

object Constants {

    val appid = "io.github.junrdev.sage"

    val auth = FirebaseAuth.getInstance()

    val usersmetadata = FirebaseFirestore.getInstance().collection("umetadata")
    val filesmetadata = FirebaseDatabase.getInstance().getReference("fmetadata")

    val filesblob = FirebaseStorage.getInstance().getReference("blob/sagefblob")
    val fileStorage = FirebaseStorage.getInstance()
    val usersblob = FirebaseStorage.getInstance().getReference("blob/sageublob")

    val IMAGE_PICK_CODE = 111
    val FILE_PICK_CODE = 222

    val favs : MutableList<FileItem> = mutableListOf()

}