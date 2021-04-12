package com.example.consumerapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.consumerapp.adapter.FollowingFollowersAdapter
import com.example.consumerapp.model.User
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray

class FollowingFragment : Fragment() {
    companion object{
        const val EXTRA_BUNDLE = "extra_bundle"
    }
    private var followingList: ArrayList<User> = arrayListOf()
    private lateinit var rvFollowing: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var followingWarning: TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_following, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        followingWarning = view.findViewById(R.id.following_warning)
        var user : User = arguments?.getParcelable<User>(EXTRA_BUNDLE) as User
        rvFollowing = view.findViewById(R.id.rv_following)
        rvFollowing.layoutManager = LinearLayoutManager(context)
        progressBar = view.findViewById(R.id.progress_bar)
        rvFollowing.setHasFixedSize(true)
        var url = "https://api.github.com/users/${user.username}/following"
        getFollowingFromJSON(url)


    }
    private fun getFollowingFromJSON(url: String){
        rvFollowing.visibility= View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        client.addHeader("Authorization", "900c99ea20e62c5c3dde2f55b2af8e4796fab26c")
        client.addHeader("User-Agent", "request")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseBody: ByteArray?
            ) {
                val result = responseBody?.let { String(it) }
                try {
                    val jsonArr = JSONArray(result)
                    if(jsonArr.length()==0){
                        followingWarning.visibility = View.VISIBLE
                    }else {
                        for (i in 0..jsonArr.length() - 1) {
                            val user = User()
                            user.avatar = jsonArr.getJSONObject(i).getString("avatar_url")
                            user.username = jsonArr.getJSONObject(i).getString("login")
                            followingList.add(user)
                        }
                    }
                    progressBar.visibility = View.INVISIBLE
                    showRecyclerList()
                    rvFollowing.visibility= View.VISIBLE

                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
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
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

            }
        })
    }
    private fun showRecyclerList() {
        val followingAdapter = FollowingFollowersAdapter(followingList)

        followingAdapter.notifyDataSetChanged()
        rvFollowing.adapter = followingAdapter

        followingAdapter.setOnItemClickCallback(object : FollowingFollowersAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })
    }
    private fun showSelectedUser(data: User) {
        val moveDetail = Intent(context, DetailActivity::class.java)
        moveDetail.putExtra(DetailActivity.EXTRA_USER, data)
        startActivity(moveDetail)
    }


}