package io.github.junrdev.sage.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.junrdev.sage.R
import io.github.junrdev.sage.activities.fragments.FilterResult
import io.github.junrdev.sage.adapter.DownloadsRecyclerAdaper
import io.github.junrdev.sage.model.FileItem
import io.github.junrdev.sage.util.Constants
import io.github.junrdev.sage.util.Constants.filesmetadata

private const val TAG = "Downloads"

class Downloads : AppCompatActivity() {

    private lateinit var categoriesRecyclerAdapter: CategoriesRecyclerAdapter
    private var files: MutableList<FileItem> = mutableListOf()
    private lateinit var filesRecycler: RecyclerView
    private lateinit var categoriesRecycler: RecyclerView

    private val categories = mutableListOf(
        "pdf",
        "Computer Science",
        "images",
        "Nursing",
        "Programming",
        "Acturial Science"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)

        filesRecycler = findViewById(R.id.filesRecycler)
        categoriesRecycler = findViewById(R.id.categoriesRecycler)
        filesRecycler.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        categoriesRecycler.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        categoriesRecyclerAdapter = CategoriesRecyclerAdapter(categories){ categoryName ->
            val filterRes = Intent(this, FilterResult::class.java)
            filterRes.putExtra("cat", categoryName)
            startActivity(filterRes)

        }
        categoriesRecycler.adapter = categoriesRecyclerAdapter

        if (!checkWritePermissions())
            requestWriteAccess()

        filesmetadata
            .get()
            .addOnCompleteListener { dataSnapshotTask ->
                if (dataSnapshotTask.isComplete && dataSnapshotTask.isSuccessful) {
                    if (dataSnapshotTask.result.hasChildren()) {

                        dataSnapshotTask.result.children
                            .forEach { snap ->
                                if (snap.exists()) {
                                    val data = snap.getValue(FileItem::class.java)!!
                                    Log.d(TAG, "onCreate: $data")
                                    files.add(data)
                                }
                            }

                        runOnUiThread {

                            if (files.isNotEmpty())
                                filesRecycler.adapter =
                                    DownloadsRecyclerAdaper(applicationContext, files)

                        }

                    }
                }
            }
            .addOnFailureListener { ex -> Log.d(TAG, "onCreate: $ex") }
    }


    private fun checkWritePermissions() =
        ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    fun requestWriteAccess() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Constants.FILE_PICK_CODE
        )
    }
}

class CategoriesRecyclerAdapter(private val categories: MutableList<String>, val onCategorySelect : (categoryName : String) -> Unit) : RecyclerView.Adapter<CategoriesRecyclerAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val category = itemView.findViewById<TextView>(R.id.categoryItem)
        private val categoryCard = itemView.findViewById<CardView>(R.id.categoryCard)

        fun bind(title: String, onClick : (cat : String) -> Unit) {
            category.text = title
            categoryCard.setOnClickListener {
                onClick(title)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.categoryitem, parent, false))

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(categories[position]){
            onCategorySelect(it)
        }
    }
}

