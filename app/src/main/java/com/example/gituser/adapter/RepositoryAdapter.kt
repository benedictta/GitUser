package com.example.gituser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gituser.R

class RepositoryAdapter(private val listRepo: ArrayList<String>) : RecyclerView.Adapter<RepositoryAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_list_repository, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val repository = listRepo[position]
        holder.txtRepository.text = repository
    }

    override fun getItemCount(): Int {
        return listRepo.size
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var txtRepository: TextView = itemView.findViewById(R.id.txt_repository)
    }
}