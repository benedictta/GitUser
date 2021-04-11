package com.example.gituser.adapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gituser.FollowerFragment
import com.example.gituser.FollowingFragment
import com.example.gituser.RepositoryFragment

class SectionsPagerAdapter(activity: AppCompatActivity, bundle: Bundle) : FragmentStateAdapter(activity) {
    private var userBundle: Bundle = bundle
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = RepositoryFragment()
            1 -> fragment = FollowingFragment()
            2 -> fragment = FollowerFragment()
        }
        fragment?.arguments = userBundle
        return fragment as Fragment
    }
}