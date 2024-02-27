package io.github.junrdev.sage.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.junrdev.sage.R
import io.github.junrdev.sage.adapter.DownloadsRecyclerAdaper
import io.github.junrdev.sage.model.FileItem
import io.github.junrdev.sage.util.Constants.filesmetadata

private const val TAG = "Downloads"

class Downloads : AppCompatActivity() {

    private lateinit var adapter: CategoriesRecyclerAdapter
    private var files: MutableList<FileItem> = mutableListOf()
    private lateinit var filesRecycler: RecyclerView
    private lateinit var categoriesRecycler: RecyclerView

    private val categories = mutableListOf<String>(
        "All",
        "Computer Science",
        "Nursing",
        "Programming",
        "Acturial Science",
        "Biology",
        "Edu",
        "Agriculture"
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
        filesRecycler.adapter = DownloadsRecyclerAdaper(applicationContext, files)

        filesmetadata
            .get()
            .addOnCompleteListener { fileTask ->
                if (fileTask.isComplete && fileTask.isSuccessful) {
                    val _files = fileTask.result

                    Log.d(TAG, "onCreate: $_files")
                    Log.d(TAG, "onCreate: ${_files.documents.size}")
                    Log.d(TAG, "onCreate: ${_files.documents}")

                    if (!_files.isEmpty) {

                        Log.d(
                            TAG,
                            "onCreate: fetched (${_files.documents.size}) -> ${_files.documents}"
                        )

                        for (doc in _files.documents)
                            doc.toObject(FileItem::class.java)?.let {
                                files.add(it)
                            }

                        if (files.isNotEmpty()){
                            adapter = CategoriesRecyclerAdapter(categories)
                            categoriesRecycler.adapter = adapter
                        }

                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "onCreate: ${it.localizedMessage}")
            }

    }

    class CategoriesRecyclerAdapter(
        private val categories: MutableList<String>
    ) : RecyclerView.Adapter<CategoriesRecyclerAdapter.VH>() {

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val radioButton = itemView.findViewById<RadioButton>(R.id.categoryItem)

            fun bind(title: String) {
                radioButton.text = title
                radioButton.setOnClickListener {
                    if (radioButton.isChecked)
                        radioButton.isChecked = !radioButton.isChecked
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.categoryitem, parent, false))

        override fun getItemCount(): Int = categories.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(categories[position])
        }
    }

}