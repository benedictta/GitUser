package com.example.gituser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gituser.adapter.LikedUserAdapter
import com.example.gituser.database.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.example.gituser.database.MappingHelper
import com.example.gituser.database.UserHelper
import com.example.gituser.model.User

class LikedUserActivity : AppCompatActivity() {
    private var likedUserList: ArrayList<User> = arrayListOf()
    private lateinit var rvUsers: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var uriWithID: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Favorite Users"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rvUsers = findViewById(R.id.rv_users)
        rvUsers.layoutManager = LinearLayoutManager(this)
        progressBar = findViewById(R.id.progress_bar)
        rvUsers.setHasFixedSize(true)
        likedUserList = getLikedUsers()
        showRecyclerList()

    }

    private fun showRecyclerList() {
        val userAdapter = LikedUserAdapter(likedUserList)

        userAdapter.notifyDataSetChanged()
        rvUsers.adapter = userAdapter

        userAdapter.setOnItemClickCallback(object : LikedUserAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })

        userAdapter.setOnLikeButtonClickCallback(object : LikedUserAdapter.OnLikeButtonClickCallback{
            override fun onLikeButtonClicked(data: User) {
                LikeButtonAction(data)
            }
        })
    }

    private fun getLikedUsers(): ArrayList<User>{
        val cursor = contentResolver.query(CONTENT_URI, null, null, null, null)
        val LikedUserList = MappingHelper.mapCursorToArrayList(cursor)
        return LikedUserList
    }

    private fun showSelectedUser(data: User) {
        val moveDetail = Intent(this@LikedUserActivity, DetailActivity::class.java)
        moveDetail.putExtra(DetailActivity.EXTRA_USER, data)
        startActivity(moveDetail)
    }

    private fun LikeButtonAction(user: User){
        val isChecked = user.isFavorite
        if(isChecked){
            removeUserFromFavorite(user)
            user.isFavorite = false
            likedUserList = getLikedUsers()
            showRecyclerList()
        }
    }

    private fun removeUserFromFavorite(user: User){
        uriWithID = Uri.parse(CONTENT_URI.toString() + "/" + user.username)
        val cursor = contentResolver.delete(uriWithID,null,null)
        if (cursor != 0) {
            Toast.makeText(this,"User Removed from Favorite", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to Remove User from Favorite", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}