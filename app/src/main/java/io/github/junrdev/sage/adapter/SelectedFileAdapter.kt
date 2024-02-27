package io.github.junrdev.sage.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.junrdev.sage.R
import io.github.junrdev.sage.model.SelectedItem

private const val TAG = "SelectedFileAdapter"

class SelectedFileAdapter(
    val context: Context,
    val selectedFiles: MutableList<SelectedItem>,
    private val onEdit : (position : Int) -> Unit
) : RecyclerView.Adapter<SelectedFileAdapter.VH>() {


    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val title = itemView.findViewById<TextView>(R.id.fileName)
        private val remove = itemView.findViewById<ImageView>(R.id.removeFile)
        private val item = itemView.findViewById<CardView>(R.id.fileItem)

        fun bind(selectedItem: SelectedItem, onRemove: () -> Unit, onEdit: () -> Unit) {
            title.text = selectedItem.fname
            remove.setOnClickListener {
                onRemove()
            }

            item.setOnClickListener { onEdit() }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.selectedfileitem, parent, false))

    override fun getItemCount(): Int = selectedFiles.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(selectedFiles[position], {
            selectedFiles.remove(selectedFiles[position])
            notifyItemRemoved(position)
        }) {
            onEdit(position)
        }
    }
}