package com.example.gituser.database

import android.provider.BaseColumns

internal class DatabaseContract {

    internal class UserColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "LikedUsers"
            const val USERNAME = "username"
            const val AVATAR_URL = "avatar_url"
        }
    }
}