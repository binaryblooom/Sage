package io.github.junrdev.sage.model

import android.net.Uri

data class SelectedItem(
    val uri : Uri,
    val fname : String,
    val categories : MutableList<String> = mutableListOf()
)
