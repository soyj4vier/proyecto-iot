package com.example.proyectoiot

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoiot.databinding.ItemUsuarioBinding
import com.example.proyectoiot.network.Usuario

class UsuarioAdapter(
    private var lista: List<Usuario>,
    private val onEditClick: (Usuario) -> Unit,
    private val onDeleteClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemUsuarioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = lista[position]
        holder.binding.tvNombre.text = usuario.nombre
        holder.binding.tvEmail.text = usuario.email
        holder.binding.tvEstado.text = usuario.estado

        // Color según estado
        when (usuario.estado) {
            "ACTIVO" -> holder.binding.tvEstado.setTextColor(Color.parseColor("#2E7D32")) // Verde
            else -> holder.binding.tvEstado.setTextColor(Color.parseColor("#C62828")) // Rojo
        }

        // Click Listeners
        holder.binding.btnEdit.setOnClickListener { onEditClick(usuario) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(usuario) }
    }

    override fun getItemCount(): Int = lista.size
    
    fun actualizarDatos(nuevaLista: List<Usuario>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}