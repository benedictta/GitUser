package com.example.gituser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.gituser.adapter.SectionsPagerAdapter
import com.example.gituser.model.User
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.lang.Exception
import java.util.*

class DetailActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_USER = "extra_user"
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.tab_text1,
            R.string.tab_text2,
            R.string.tab_text3
        )

    }
    private lateinit var progressDialog: AlertDialog
    private lateinit var userDataLayout: ConstraintLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val user: User = intent.getParcelableExtra(EXTRA_USER) as User

        showUserData(user)
        supportActionBar?.elevation = 0f

    }
    fun loading(status: Boolean, progressDialog: AlertDialog){
        userDataLayout = findViewById(R.id.userDataLayout)
        tabLayout = findViewById(R.id.tabs)
        viewPager = findViewById(R.id.view_pager)
        if(status){
            userDataLayout.visibility = View.INVISIBLE
            tabLayout.visibility = View.INVISIBLE
            viewPager.visibility = View.INVISIBLE
            progressDialog.show()
        }
        if(!status){
            userDataLayout.visibility = View.VISIBLE
            tabLayout.visibility = View.VISIBLE
            viewPager.visibility = View.VISIBLE
            Timer().schedule(object : TimerTask(){
                override fun run() {
                    progressDialog.dismiss()
                }
            },1500)
        }
    }
    private fun showUserData(user: User){
        progressDialog = createProgressDialog()
        loading(true, progressDialog)
        val txtUsername: TextView = findViewById(R.id.txt_name)
        val userAvatar: ImageView = findViewById(R.id.user_avatar)
        val txtCompany: TextView = findViewById(R.id.txt_company)
        val txtLocation: TextView = findViewById(R.id.txt_location)

        val client = AsyncHttpClient()
        client.addHeader("Authorization", "900c99ea20e62c5c3dde2f55b2af8e4796fab26c")
        client.addHeader("User-Agent", "request")
        val url = "https://api.github.com/users/${user.username}"
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                val result = responseBody?.let { String(it) }
                try {
                    val jsonObj = JSONObject(result)
                    user.name = if(jsonObj.getString("name")=="null"){user.username}else{jsonObj.getString("name")}
                    user.company = if(jsonObj.getString("company")=="null"){"-"}else{jsonObj.getString("company")}
                    user.location = if(jsonObj.getString("location")=="null"){"-"}else{jsonObj.getString("location")}
                    user.repository = jsonObj.getInt("public_repos")
                    user.follower = jsonObj.getInt("followers")
                    user.following = jsonObj.getInt("following")
                    user.repositoryLink = jsonObj.getString("repos_url")
                    user.followerLink = jsonObj.getString("followers_url")
                    user.followingLink = jsonObj.getString("following_url")

                    supportActionBar?.title = user.username
                    Glide.with(this@DetailActivity).load(user.avatar).into(userAvatar)
                    txtUsername.setText(user.name)
                    txtCompany.setText(user.company)
                    txtLocation.setText(user.location)

                    val userBundle = Bundle()
                    userBundle.putParcelable("extra_bundle",user)
                    val sectionsPagerAdapter = SectionsPagerAdapter(this@DetailActivity, userBundle)
                    val viewPager: ViewPager2 = findViewById(R.id.view_pager)
                    viewPager.adapter = sectionsPagerAdapter
                    val tabs: TabLayout = findViewById(R.id.tabs)
                    TabLayoutMediator(tabs, viewPager) { tab, position ->
                        tab.text = resources.getString(TAB_TITLES[position])
                    }.attach()
                    loading(false,progressDialog)

                } catch (e: Exception) {
                    Toast.makeText(this@DetailActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error?.message}"
                }
                Toast.makeText(this@DetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun createProgressDialog():AlertDialog{
        val dialog: AlertDialog
        val activity = this
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_loading_dialog,null))
        builder.setCancelable(true)
        dialog = builder.create()
        return dialog
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}