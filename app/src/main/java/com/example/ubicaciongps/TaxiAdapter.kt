package com.example.ubicaciongps

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class TaxiAdapter(private val taxis: List<Taxi>) : RecyclerView.Adapter<TaxiAdapter.TaxiViewHolder>() {

    class TaxiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMovil: TextView = view.findViewById(R.id.tvMovil)
        val tvCarnet: TextView = view.findViewById(R.id.tvCarnet)
        val tvLatitud: TextView = view.findViewById(R.id.tvLatitud)
        val tvLongitud: TextView = view.findViewById(R.id.tvLongitud)
        val btnMapa: MaterialButton = view.findViewById(R.id.btnMapa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_taxi, parent, false)
        return TaxiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaxiViewHolder, position: Int) {
        val taxi = taxis[position]
        holder.tvMovil.text = "Móvil: ${taxi.movil}"
        holder.tvCarnet.text = "Carnet: ${taxi.carnet}"
        holder.tvLatitud.text = "Lat: ${taxi.latitud}"
        holder.tvLongitud.text = "Lon: ${taxi.longitud}"

        holder.btnMapa.setOnClickListener {
            val uri = "https://www.google.com/maps/search/?api=1&query=${taxi.latitud},${taxi.longitud}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = taxis.size
}