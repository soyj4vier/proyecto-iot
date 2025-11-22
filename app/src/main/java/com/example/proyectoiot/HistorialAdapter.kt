package com.example.proyectoiot

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoiot.databinding.ItemHistorialBinding
import com.example.proyectoiot.network.Evento

class HistorialAdapter(private var lista: List<Evento>) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistorialBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evento = lista[position]
        
        holder.binding.tvFecha.text = evento.fechaHora
        holder.binding.tvDetalle.text = "Origen: ${evento.origen}"

        if (evento.resultado == "PERMITIDO") {
            holder.binding.ivStatus.setImageResource(android.R.drawable.checkbox_on_background)
            holder.binding.ivStatus.setColorFilter(Color.parseColor("#2E7D32")) // Verde
            
            // Título más amigable
            holder.binding.tvEvento.text = when(evento.tipoEvento) {
                "ACCESO_VALIDO" -> "Acceso Autorizado"
                "APERTURA_MANUAL" -> "Apertura Remota"
                else -> evento.tipoEvento
            }
        } else {
            holder.binding.ivStatus.setImageResource(android.R.drawable.ic_delete)
            holder.binding.ivStatus.setColorFilter(Color.parseColor("#C62828")) // Rojo
            
             holder.binding.tvEvento.text = when(evento.tipoEvento) {
                "ACCESO_RECHAZADO" -> "Acceso Denegado"
                else -> evento.tipoEvento
            }
        }
    }

    override fun getItemCount(): Int = lista.size
    
    fun actualizarDatos(nuevaLista: List<Evento>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}