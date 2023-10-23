package com.abdialidrus.facerecognition

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class CroppedFacesAdapter(private val croppedFaceList: List<Bitmap>, private val interaction: Interaction): RecyclerView.Adapter<CroppedFacesAdapter.CroppedFaceViewHolder>() {

    interface Interaction {
        fun onImageClicked(bitmap: Bitmap)
    }

    class CroppedFaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_cropped)
        fun bind(bitmap: Bitmap, interaction: Interaction) {
            imageView.setImageBitmap(bitmap)
            imageView.setOnClickListener {
                interaction.onImageClicked(bitmap)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CroppedFaceViewHolder {
        val viewLayout = LayoutInflater.from(parent.context).inflate(
            R.layout.item_cropped_face,
            parent,false)
        return CroppedFaceViewHolder(viewLayout)
    }

    override fun getItemCount(): Int {
        return croppedFaceList.size
    }

    override fun onBindViewHolder(holder: CroppedFaceViewHolder, position: Int) {
        val imageBitmap = croppedFaceList[position]
        holder.bind(imageBitmap, interaction)
    }
}