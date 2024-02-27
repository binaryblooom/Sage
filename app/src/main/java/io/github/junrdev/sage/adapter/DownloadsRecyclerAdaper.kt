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
import io.github.junrdev.sage.util.Shared
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    val client = OkHttpClient()

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val preview = itemView.findViewById<ImageView>(R.id.filePreview)
        val optionsMenu = itemView.findViewById<ImageView>(R.id.fileOptions)
        val title = itemView.findViewById<TextView>(R.id.fileTitle)
        val type = itemView.findViewById<TextView>(R.id.fileType)

        fun bind(fileItem: FileItem) {
            if (fileItem.filePreview != null)
                Picasso.get()
                    .load(fileItem.filePreview)
                    .placeholder(R.drawable.round_insert_drive_file_24)
                    .into(preview)

            title.text = fileItem.fileName
            type.text = fileItem.fileType

            optionsMenu.setOnClickListener {
                val pop = PopupMenu(context, it)
                pop
                    .menuInflater
                    .inflate(R.menu.fileoptionmenu, pop.menu)

                pop.setOnMenuItemClickListener {menu->
                    when (menu.itemId) {
                        R.id.favourite -> {
                            Shared.addToFavs.add(fileItem.fileDownloadUrl)
                            return@setOnMenuItemClickListener true
                        }

                        R.id.download -> {
                            CoroutineScope(Dispatchers.IO).launch {
//                                invoke download
                                val saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                val downloadedFile = File(saveDir, fileItem.fileName)

                                Log.d(TAG, "bind: $fileItem")

                                val req  = Request.Builder()
                                    .url(fileItem.fileDownloadUrl)
                                    .build()

                                Log.d(TAG, "bind: $req")

                                client.newCall(req)
                                    .enqueue(object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            Toast.makeText(context, "Failed please try again.", Toast.LENGTH_SHORT).show()
                                            Log.d(TAG, "onFailure: ${e.localizedMessage}")
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            if (response.isSuccessful){

                                                val inputStream = response.body?.byteStream()
                                                val op = FileOutputStream(downloadedFile)

                                                Log.d(TAG, "onResponse: $op")

                                                op?.use {p->
                                                    p.write(response.body!!.bytes())
                                                    Toast.makeText(context, "File downloaded check downloads folder.", Toast.LENGTH_SHORT).show()

                                                }

                                                /*
                                                inputStream?.use {inp ->
                                                    op.use { oup ->
                                                        inp.copyTo(oup)
                                                        Toast.makeText(context, "File downloaded check downloads folder.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                */

                                            }else{
                                                Toast.makeText(context, "Failed please try again.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    })

                            }
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

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.fileitem, parent, false))
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(files[position])
    }
}