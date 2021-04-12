package com.example.consumerapp

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
import com.example.consumerapp.model.User
import com.example.consumerapp.adapter.RepositoryAdapter
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray

class RepositoryFragment : Fragment() {
    companion object{
        const val EXTRA_BUNDLE = "extra_bundle"
    }
    private var repoList: ArrayList<String> = arrayListOf()
    private lateinit var rvRepo: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var repositoryWarning: TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_repository, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repositoryWarning = view.findViewById(R.id.repository_warning)
        var user : User = arguments?.getParcelable<User>(EXTRA_BUNDLE) as User
        rvRepo = view.findViewById(R.id.rv_repository)
        rvRepo.layoutManager = LinearLayoutManager(context)
        progressBar = view.findViewById(R.id.progress_bar)
        rvRepo.setHasFixedSize(true)
        var url = user.repositoryLink
        getRepoFromJSON(url)


    }
    private fun getRepoFromJSON(url: String){
        rvRepo.visibility= View.INVISIBLE
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
                        repositoryWarning.visibility = View.VISIBLE
                    }else {
                        for (i in 0..jsonArr.length() - 1) {
                            val repo = jsonArr.getJSONObject(i).getString("name")
                            repoList.add(repo)
                        }
                    }
                    progressBar.visibility = View.INVISIBLE
                    showRecyclerList()
                    rvRepo.visibility= View.VISIBLE

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
        val userAdapter = RepositoryAdapter(repoList)
        userAdapter.notifyDataSetChanged()
        rvRepo.adapter = userAdapter
    }
}