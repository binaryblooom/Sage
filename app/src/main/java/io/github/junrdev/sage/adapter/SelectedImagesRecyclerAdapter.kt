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
import com.squareup.picasso.Picasso
import io.github.junrdev.sage.R
import io.github.junrdev.sage.model.SelectedItem

private const val TAG = "SelectedImagesRecyclerA"

class SelectedImagesRecyclerAdapter(
    private val selectedImages: MutableList<SelectedItem>,
    private val context: Context,
    private val onEdit: (position : Int) -> Unit
) : RecyclerView.Adapter<SelectedImagesRecyclerAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val selectedImageCard = itemView.findViewById<CardView>(R.id.selectedImageCard)
        private val selectedImage = itemView.findViewById<ImageView>(R.id.selectedImage)
        private val removeImage = itemView.findViewById<ImageView>(R.id.removeImage)
        private val editImage = itemView.findViewById<ImageView>(R.id.editImage)
        private val selectedImageName = itemView.findViewById<TextView>(R.id.selectedImageName)

        fun bind(image: SelectedItem, onRemove: () -> Unit, onEdit: () -> Unit) {
            Log.d(TAG, "binding: $image")
            Picasso.get()
                .load(image.uri)
                .into(selectedImage)

            removeImage.setOnClickListener { onRemove() }
            selectedImageName.text = image.fname

            editImage.setOnClickListener { onEdit() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.selectedimageitem, parent, false)
        )
    }

    override fun getItemCount(): Int = selectedImages.size


    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(selectedImages[position], {
            selectedImages.remove(selectedImages[position])
            notifyItemRemoved(position)
        }){
            onEdit(position)
        }
    }

//    inner class AddBgToPic: AsyncTask<Void, Bitmap, Pale>
}