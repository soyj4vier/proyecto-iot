package com.example.proyectoiot

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoiot.databinding.ItemSensorBinding
import com.example.proyectoiot.network.Sensor

class SensorAdapter(
    private var lista: List<Sensor>,
    private val onEditClick: (Sensor) -> Unit,
    private val onDeleteClick: (Sensor) -> Unit
) : RecyclerView.Adapter<SensorAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSensorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSensorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sensor = lista[position]
        holder.binding.tvCodigo.text = sensor.codigo
        holder.binding.tvTipo.text = sensor.tipo
        holder.binding.tvEstado.text = sensor.estado

        // Cambiar color según estado
        when (sensor.estado) {
            "ACTIVO" -> holder.binding.tvEstado.setTextColor(Color.parseColor("#2E7D32")) // Verde
            "INACTIVO" -> holder.binding.tvEstado.setTextColor(Color.parseColor("#757575")) // Gris
            "BLOQUEADO" -> holder.binding.tvEstado.setTextColor(Color.parseColor("#C62828")) // Rojo
            "PERDIDO" -> holder.binding.tvEstado.setTextColor(Color.parseColor("#F9A825")) // Amarillo
        }
        
        // Icono según tipo
        if (sensor.tipo == "LLAVERO") {
            holder.binding.ivIcon.setImageResource(android.R.drawable.ic_menu_mylocation) // Icono temporal para llavero
        } else {
            holder.binding.ivIcon.setImageResource(android.R.drawable.ic_menu_compass) // Icono temporal para tarjeta
        }

        // Click Listeners para Editar y Eliminar
        holder.binding.btnEditSensor.setOnClickListener { onEditClick(sensor) }
        holder.binding.btnDeleteSensor.setOnClickListener { onDeleteClick(sensor) }
    }

    override fun getItemCount(): Int = lista.size
    
    fun actualizarDatos(nuevaLista: List<Sensor>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}