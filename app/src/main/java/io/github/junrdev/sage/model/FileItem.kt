package io.github.junrdev.sage.model

import java.util.Collections.emptyList
import java.util.Date

data class FileItem(
    var fileId: String = "",
    val fileName: String = "",
    val fileDownloadUrl: String = "",
    val filePreview: String? = null,
    val fileType: String = "",
    val uploadedBy: String = "",
    val uploadDate: String = Date(System.currentTimeMillis()).toString(),
    val downloads: Int = 0,
    val categories: List<String> = emptyList()
)