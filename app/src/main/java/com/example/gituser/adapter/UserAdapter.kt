package com.example.gituser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gituser.R
import com.example.gituser.model.User

class UserAdapter(private val listUser: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.ListViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback
    private lateinit var onLikeButtonClickCallback: OnLikeButtonClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    fun setOnLikeButtonClickCallback(onLikeButtonClickCallback: OnLikeButtonClickCallback){
        this.onLikeButtonClickCallback = onLikeButtonClickCallback
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_list_user, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val user = listUser[position]
        Glide.with(holder.itemView.context)
            .load(user.avatar)
            .apply(RequestOptions().override(80, 80))
            .into(holder.imgPhoto)
        holder.txtUsername.text = user.username
        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(listUser[holder.adapterPosition]) }
        if(!user.isFavorite)
            holder.likeButton.isChecked = false
        else if(user.isFavorite)
            holder.likeButton.isChecked = true
        holder.likeButton.setOnClickListener { onLikeButtonClickCallback.onLikeButtonClicked(listUser[holder.adapterPosition])}
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var txtUsername: TextView = itemView.findViewById(R.id.txt_user_name)
        var imgPhoto: ImageView = itemView.findViewById(R.id.img_item_photo)
        var likeButton: CheckBox = itemView.findViewById(R.id.like_button)
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: User)
    }

    interface OnLikeButtonClickCallback {
        fun onLikeButtonClicked(data: User)
    }


}