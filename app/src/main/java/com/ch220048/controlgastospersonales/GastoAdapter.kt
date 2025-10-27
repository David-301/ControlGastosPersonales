package com.ch220048.controlgastospersonales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GastoAdapter(
    private var listaGastos: List<Gasto>,
    private val onGastoClick: (Gasto) -> Unit = {} // Click listener
) : RecyclerView.Adapter<GastoAdapter.GastoViewHolder>() {

    class GastoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcono: ImageView = itemView.findViewById(R.id.ivIcono)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvNotas: TextView = itemView.findViewById(R.id.tvNotas)
        val tvMonto: TextView = itemView.findViewById(R.id.tvMonto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gasto, parent, false)
        return GastoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        val gasto = listaGastos[position]

        holder.tvNombre.text = gasto.nombre
        holder.tvCategoria.text = gasto.categoria
        holder.tvFecha.text = gasto.fecha
        holder.tvMonto.text = "$${String.format("%.2f", gasto.monto)}"

        // Mostrar notas solo si existen
        if (gasto.notas.isNotEmpty()) {
            holder.tvNotas.visibility = View.VISIBLE
            holder.tvNotas.text = gasto.notas
        } else {
            holder.tvNotas.visibility = View.GONE
        }

        // Cambiar icono según categoría
        val iconoRes = when (gasto.categoria) {
            "Comida" -> android.R.drawable.ic_menu_sort_by_size
            "Transporte" -> android.R.drawable.ic_menu_directions
            "Entretenimiento" -> android.R.drawable.ic_menu_camera
            "Salud" -> android.R.drawable.ic_menu_add
            "Educación" -> android.R.drawable.ic_menu_edit
            "Servicios" -> android.R.drawable.ic_menu_manage
            "Compras" -> android.R.drawable.ic_menu_gallery
            "Vivienda" -> android.R.drawable.ic_menu_myplaces
            else -> android.R.drawable.ic_menu_info_details
        }
        holder.ivIcono.setImageResource(iconoRes)

        // Click listener para editar
        holder.itemView.setOnClickListener {
            onGastoClick(gasto)
        }
    }

    override fun getItemCount(): Int = listaGastos.size

    // Función para actualizar la lista
    fun actualizarLista(nuevaLista: List<Gasto>) {
        listaGastos = nuevaLista
        notifyDataSetChanged()
    }
}