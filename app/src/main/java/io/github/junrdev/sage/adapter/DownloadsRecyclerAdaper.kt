package io.github.junrdev.sage.adapter

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.github.junrdev.sage.R
import io.github.junrdev.sage.model.FileItem
import io.github.junrdev.sage.util.Constants
import io.github.junrdev.sage.util.Constants.fileStorage
import io.github.junrdev.sage.util.Constants.filesmetadata
import io.github.junrdev.sage.util.Shared
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "DownloadsRecyclerAdaper"

class DownloadsRecyclerAdaper(
    val context: Context,
    val files: MutableList<FileItem>,
) : RecyclerView.Adapter<DownloadsRecyclerAdaper.VH>() {


    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val preview = itemView.findViewById<ImageView>(R.id.filePreview)
        val optionsMenu = itemView.findViewById<ImageView>(R.id.fileOptions)
        val title = itemView.findViewById<TextView>(R.id.fileTitle)
        val type = itemView.findViewById<TextView>(R.id.fileType)

        fun bind(fileItem: FileItem) {

            title.text = fileItem.fileName
            type.text = fileItem.fileType

            val prev = when (fileItem.fileType) {
                "image" -> R.drawable.picturepreview
                else -> R.drawable.pdfpreview
            }

            Picasso.get()
                .load(prev)
                .into(preview)

            optionsMenu.setOnClickListener {
                val pop = PopupMenu(context, it)
                pop
                    .menuInflater
                    .inflate(R.menu.fileoptionmenu, pop.menu)

                pop.setOnMenuItemClickListener { menu ->
                    when (menu.itemId) {
                        R.id.favourite -> {
                            Constants.favs.add(fileItem)
                            return@setOnMenuItemClickListener true
                        }

                        R.id.download -> {

                            val client = OkHttpClient()
                            val request = Request.Builder()
                                .url(fileItem.fileDownloadUrl)
                                .build()

                            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()

                            client.newCall(request)
                                .enqueue(object : Callback{
                                    override fun onFailure(call: Call, e: IOException) {
                                        Log.d(TAG, "onFailure: Failed ${e.localizedMessage}")
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        if (response.isSuccessful){

                                            Log.d(TAG, "onResponse: ${response.body}")

                                            val localfile = File(
                                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                                fileItem.fileName
                                            )

                                            FileOutputStream(localfile).use {output->
                                                response.body?.byteStream().use {input ->
                                                    input?.copyTo(output)
                                                }
                                            }
//                                            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
                                            Log.d(TAG, "onResponse: Done")
                                        }
                                    }
                                })

                            Toast.makeText(context, " Downloading File.", Toast.LENGTH_SHORT).show()

                            return@setOnMenuItemClickListener true
//
                        }

                        R.id.wrongDetails -> {
                            Toast.makeText(context, "Coming soon!!.", Toast.LENGTH_SHORT).show()
                            return@setOnMenuItemClickListener true
                        }

                        else -> return@setOnMenuItemClickListener false
                    }
                }

                pop.show()
            }
        }

        private fun updateFileInfo(fileItem: FileItem) {
            filesmetadata.child(fileItem.fileId)
                .updateChildren(mapOf(Pair("downloads", ++fileItem.downloads)))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.isComplete) {

                    }
                }.addOnFailureListener {
                    Log.d(TAG, "updateFileInfo: Failed ${it.localizedMessage}")
                }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.fileitem, parent, false))
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(files[position])
    }
}