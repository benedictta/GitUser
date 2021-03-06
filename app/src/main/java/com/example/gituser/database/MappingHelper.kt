package com.example.gituser.database

import android.database.Cursor
import com.example.gituser.model.User

object MappingHelper {

    fun mapCursorToArrayList(userCursor: Cursor?): ArrayList<User> {
        val userList = ArrayList<User>()
        userCursor?.apply {
            while (moveToNext()) {
                val user = User()
                val username = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.USERNAME))
                val avatar_url = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.AVATAR_URL))
                user.username = username
                user.avatar = avatar_url
                user.isFavorite = true
                userList.add(user)
            }
        }
        return userList
    }
}