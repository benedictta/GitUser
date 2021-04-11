package com.example.gituser

import android.app.SearchManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import com.example.gituser.adapter.UserAdapter
import com.example.gituser.database.DatabaseContract
import com.example.gituser.database.MappingHelper
import com.example.gituser.database.UserHelper
import com.example.gituser.model.User
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private var userList: ArrayList<User> = arrayListOf()
    private var searchList: ArrayList<User> = arrayListOf()
    private lateinit var rvUsers: RecyclerView
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Github User"
        rvUsers = findViewById(R.id.rv_users)
        rvUsers.layoutManager = LinearLayoutManager(this)
        progressBar = findViewById(R.id.progress_bar)
        rvUsers.setHasFixedSize(true)
        getUsersFromJSON()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menus, menu)
        val likedUserMenuItem = menu.findItem(R.id.fav_user)
        val settingMenuItem = menu.findItem(R.id.setting)
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
                likedUserMenuItem.setVisible(false)
                settingMenuItem.setVisible(false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                likedUserMenuItem.setVisible(true)
                settingMenuItem.setVisible(true)
                showRecyclerList()
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.fav_user -> {
                val moveToLikedUser = Intent(this@MainActivity, LikedUserActivity::class.java)
                startActivity(moveToLikedUser)
            }
            R.id.setting -> {
                val moveToSetting = Intent(this@MainActivity, SettingActivity::class.java)
                startActivity(moveToSetting)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showRecyclerList() {
        val userAdapter = UserAdapter(userList)

        userAdapter.notifyDataSetChanged()
        rvUsers.adapter = userAdapter

        userAdapter.setOnItemClickCallback(object : UserAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })

        userAdapter.setOnLikeButtonClickCallback(object : UserAdapter.OnLikeButtonClickCallback{
            override fun onLikeButtonClicked(data: User) {
                LikeButtonAction(data)
            }
        })
    }

    private fun showSearchResult() {
        val userAdapter = UserAdapter(searchList)
        userAdapter.notifyDataSetChanged()
        rvUsers.adapter = userAdapter

        userAdapter.setOnItemClickCallback(object : UserAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })
        userAdapter.setOnLikeButtonClickCallback(object : UserAdapter.OnLikeButtonClickCallback{
            override fun onLikeButtonClicked(data: User) {
                LikeButtonAction(data)
            }
        })
    }

    private fun isFavoriteUser(username: String): Boolean{
        val userHelper = UserHelper.getInstance(applicationContext)
        userHelper.open()
        val isFavorite = userHelper.checkIfExist(username)
        userHelper.close()
        return isFavorite
    }

    private fun LikeButtonAction(user: User){
        val isChecked = user.isFavorite
        if(isChecked){
            removeUserFromFavorite(user)
            user.isFavorite = false
        }
        else if(!isChecked){
            addUserToFavorite(user)
            user.isFavorite = true
        }
    }

    private fun addUserToFavorite(user: User){
        val userHelper = UserHelper.getInstance(applicationContext)
        userHelper.open()
        val values = ContentValues()
        values.put(DatabaseContract.UserColumns.USERNAME, user.username)
        values.put(DatabaseContract.UserColumns.AVATAR_URL, user.avatar)
        val result = userHelper.insert(values)
        if (result > 0) {
            Toast.makeText(this,"User Added to Favorite",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to Add User to Favorite", Toast.LENGTH_SHORT).show()
        }
        userHelper.close()
    }

    private fun removeUserFromFavorite(user: User){
        val userHelper = UserHelper.getInstance(applicationContext)
        userHelper.open()
        val result = userHelper.deleteById(user.username).toLong()
        if (result > 0) {
            Toast.makeText(this,"User Removed from Favorite",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to Remove User from Favorite", Toast.LENGTH_SHORT).show()
        }
        userHelper.close()
    }

    private fun getUsersFromJSON(){
        rvUsers.visibility= View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        client.addHeader("Authorization", "900c99ea20e62c5c3dde2f55b2af8e4796fab26c")
        client.addHeader("User-Agent", "request")
        val url = "https://api.github.com/users?per_page=100"
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                val result = responseBody?.let { String(it) }
                try {
                    val jsonArr = JSONArray(result)
                    for (i in 0..jsonArr.length() - 1) {
                        val user = User()
                        user.avatar = jsonArr.getJSONObject(i).getString("avatar_url")
                        user.username = jsonArr.getJSONObject(i).getString("login")
                        if(isFavoriteUser(user.username)){
                            user.isFavorite = true
                        }
                        userList.add(user)
                    }
                    progressBar.visibility = View.INVISIBLE
                    showRecyclerList()
                    rvUsers.visibility= View.VISIBLE

                } catch (e: Exception) {
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
                        if(isFavoriteUser(user.username)){
                            user.isFavorite = true
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
    private fun showSelectedUser(data: User) {
        val moveDetail = Intent(this@MainActivity, DetailActivity::class.java)
        moveDetail.putExtra(DetailActivity.EXTRA_USER, data)
        startActivity(moveDetail)
    }
}