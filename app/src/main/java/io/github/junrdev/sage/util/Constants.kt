package io.github.junrdev.sage.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object Constants {

    val appid = "io.github.junrdev.sage"

    val auth = FirebaseAuth.getInstance()

    val usersmetadata = FirebaseFirestore.getInstance().collection("umetadata")
    val filesmetadata = FirebaseFirestore.getInstance().collection("fmetadata")

    val filesblob = FirebaseStorage.getInstance().getReference("blob/sagefblob")
    val usersblob = FirebaseStorage.getInstance().getReference("blob/sageublob")

    val IMAGE_PICK_CODE = 111
    val FILE_PICK_CODE = 222

}