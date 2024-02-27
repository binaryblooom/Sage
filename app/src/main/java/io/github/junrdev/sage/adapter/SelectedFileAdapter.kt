package io.github.junrdev.sage.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.junrdev.sage.R
import io.github.junrdev.sage.model.SelectedItem

class SelectedFileAdapter (
    val context : Context,
    val selectedFiles : MutableList<SelectedItem>
) : RecyclerView.Adapter<SelectedFileAdapter.VH> (){


    class VH (itemView : View): RecyclerView.ViewHolder(itemView){

        private val title = itemView.findViewById<TextView>(R.id.fileName)
        private val remove = itemView.findViewById<ImageView>(R.id.removeFile)

        fun bind(selectedItem: SelectedItem, onRemove: () -> Unit) {
            title.text = selectedItem.fname
            remove.setOnClickListener {
                onRemove()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    =VH(LayoutInflater.from(parent.context).inflate(R.layout.selectedfileitem, parent, false))

    override fun getItemCount(): Int = selectedFiles.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(selectedFiles[position]){
            selectedFiles.remove(selectedFiles[position])
            notifyItemRemoved(position)
        }
    }
}