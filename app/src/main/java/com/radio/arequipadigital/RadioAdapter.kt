package com.radio.arequipadigital

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RadioAdapter(
    private val radioList: List<RadioStation>,
    private val onClick: (RadioStation) -> Unit
) : RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    class RadioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioImage: ImageView = itemView.findViewById(R.id.radio_image)
        val radioName: TextView = itemView.findViewById(R.id.radio_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.radio_item, parent, false)
        return RadioViewHolder(view)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {
        val station = radioList[position]

        // Usar Glide para cargar la imagen desde la URL
        Glide.with(holder.itemView.context)
            .load(station.imageUrl)
            .placeholder(R.drawable.placeholder_image) // Imagen por defecto mientras se carga
            .error(R.drawable.error_image)  // Imagen si falla la carga
            .into(holder.radioImage)

        // Establecer el nombre de la estación de radio
        holder.radioName.text = station.name

        // Manejar el clic del usuario
        holder.itemView.setOnClickListener { onClick(station) }
    }

    override fun getItemCount() = radioList.size
}