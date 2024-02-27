package io.github.junrdev.sage.model

import io.github.junrdev.sage.util.Constants
import java.util.Collections.emptyList

data class User(
    val appId: String = Constants.appid,
    val uid: String,
    val favourites: List<String> = emptyList(),
    val uploads: MutableList<String> = mutableListOf(),
    val downloads: List<String> = emptyList()
)

