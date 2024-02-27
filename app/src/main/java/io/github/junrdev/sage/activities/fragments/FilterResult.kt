package io.github.junrdev.sage.activities.fragments

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.junrdev.sage.R
import io.github.junrdev.sage.adapter.DownloadsRecyclerAdaper
import io.github.junrdev.sage.model.FileItem
import io.github.junrdev.sage.util.Constants
import io.github.junrdev.sage.util.Constants.favs

private const val TAG = "FilterResult"
class FilterResult : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var filesRecycler : RecyclerView
    private val files : MutableList<FileItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_result)
        toolbar = findViewById(R.id.toolbar)
        filesRecycler = findViewById(R.id.filesRecycler)
        setSupportActionBar(toolbar)


        filesRecycler.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

        if (intent.hasExtra("cat")){
            val cat = intent.getStringExtra("cat")
            toolbar.subtitle = "Results for $cat"

            Constants.filesmetadata
                .get()
                .addOnCompleteListener {datasnap ->

                    if (datasnap.isSuccessful && datasnap.isComplete){

                        val res = datasnap.result.children

                        res.forEach { data ->
                            if (data.exists()){
                                files.add(data.getValue(FileItem::class.java)!!)
                            }
                        }

                        runOnUiThread {
                            if (files.isNotEmpty()){

                                Log.d(TAG, "onCreate: unfiltered $files")

                                Log.d(TAG, "onCreate: filtered $files")
                                filesRecycler.adapter = DownloadsRecyclerAdaper(applicationContext, files = files.filter { file -> file.categories.contains(cat) }.toMutableList())
                            }
                        }
                    }

                }.addOnFailureListener {
                    Log.d(TAG, "onCreate: ${it.localizedMessage}")
                }

        }

        else if (intent.hasExtra("content") && intent.getStringExtra("content").equals("favs")){

            toolbar.title = "Favourites"
            toolbar.subtitle = "Your Saved Documents"
            Log.d(TAG, "onCreate: Favs $favs")
            filesRecycler.adapter = DownloadsRecyclerAdaper(applicationContext, favs)
        }
    }
}