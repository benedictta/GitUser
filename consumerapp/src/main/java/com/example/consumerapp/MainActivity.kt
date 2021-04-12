package com.example.consumerapp

import android.app.SearchManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.consumerapp.adapter.LikedUserAdapter
import com.example.consumerapp.database.DatabaseContract
import com.example.consumerapp.database.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.example.consumerapp.helper.MappingHelper
import com.example.consumerapp.model.User
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var likedUserList: ArrayList<User> = arrayListOf()
    private var searchList: ArrayList<User> = arrayListOf()
    private lateinit var rvUsers: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var uriWithID: Uri
    private var isSearch = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Consumer App"
        rvUsers = findViewById(R.id.rv_users)
        rvUsers.layoutManager = LinearLayoutManager(this)
        progressBar = findViewById(R.id.progress_bar)
        rvUsers.setHasFixedSize(true)
        showLikedUsers()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menus, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView as SearchView
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        searchView.maxWidth =  displayMetrics.widthPixels
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = resources.getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                isSearch = true
                searchUserFromJSON(query)
                searchView.clearFocus();
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {

                return false
            }
        })
        searchMenuItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                isSearch = false
                showLikedUsers()
                return true
            }
        })
        return true
    }
    private fun showLikedUsers() {
        likedUserList = getLikedUsers()
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
        val moveDetail = Intent(this@MainActivity, DetailActivity::class.java)
        moveDetail.putExtra(DetailActivity.EXTRA_USER, data)
        startActivity(moveDetail)
    }

    private fun LikeButtonAction(user: User){
        val isChecked = user.isFavorite
        if(isChecked){
            removeUserFromFavorite(user)
            likedUserList.remove(user)
            user.isFavorite = false
            if(!isSearch){
                showLikedUsers()
            }
        }
        else if(!isChecked){
            addUserToFavorite(user)
            likedUserList.add(user)
            user.isFavorite = true
        }
    }

    private fun addUserToFavorite(user: User){
        val values = ContentValues()
        values.put(DatabaseContract.UserColumns.USERNAME, user.username)
        values.put(DatabaseContract.UserColumns.AVATAR_URL, user.avatar)
        val cursor = contentResolver.insert(CONTENT_URI, values)
        if (cursor?.lastPathSegment != "0") {
            Toast.makeText(this,"User Added to Favorite",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to Add User to Favorite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeUserFromFavorite(user: User){
        uriWithID = Uri.parse(DatabaseContract.UserColumns.CONTENT_URI.toString() + "/" + user.username)
        val cursor = contentResolver.delete(uriWithID,null,null)
        if (cursor != 0) {
            Toast.makeText(this,"User Removed from Favorite", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to Remove User from Favorite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSearchResult() {
        val userAdapter = LikedUserAdapter(searchList)
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

    private fun searchUserFromJSON(query: String){
        searchList.clear()
        rvUsers.visibility= View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        client.addHeader("Authorization", "900c99ea20e62c5c3dde2f55b2af8e4796fab26c")
        client.addHeader("User-Agent", "request")
        val url = "https://api.github.com/search/users?q=$query"
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                val result = responseBody?.let { String(it) }
                try {
                    val jsonObj = JSONObject(result)
                    val jsonArr = jsonObj.getJSONArray("items")
                    for (i in 0..jsonArr.length() - 1) {
                        val user = User()
                        user.avatar = jsonArr.getJSONObject(i).getString("avatar_url")
                        user.username = jsonArr.getJSONObject(i).getString("login")
                        user.isFavorite = true
                        if(!isFavoriteUser(user)){
                            user.isFavorite = false
                        }
                        searchList.add(user)
                    }
                    progressBar.visibility = View.INVISIBLE
                    rvUsers.visibility = View.VISIBLE
                    showSearchResult()

                } catch (e: java.lang.Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error?.message}"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isFavoriteUser(user: User): Boolean{
        var isFavorite = false
        if(likedUserList.contains(user)) {
            isFavorite = true
        }
        return isFavorite
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}