package com.example.gituser.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.example.gituser.database.DatabaseContract.UserColumns.Companion.TABLE_NAME
import com.example.gituser.database.DatabaseContract.UserColumns.Companion.USERNAME

class UserHelper(context: Context) {

    companion object {
        private const val DATABASE_TABLE = TABLE_NAME
        private lateinit var dataBaseHelper: DatabaseHelper
        private lateinit var database: SQLiteDatabase
        private var INSTANCE: UserHelper? = null

        fun getInstance(context: Context): UserHelper =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: UserHelper(context)
                }
    }

    init {
        dataBaseHelper = DatabaseHelper(context)
    }

    @Throws(SQLException::class)
    fun open() {
        database = dataBaseHelper.writableDatabase
    }
    fun close() {
        dataBaseHelper.close()
        if (database.isOpen)
            database.close()
    }

    fun queryAll(): Cursor {
        return database.query(
                DATABASE_TABLE,
                null,
                null,
                null,
                null,
                null,
                "USERNAME ASC")
    }

    fun queryByUser(username: String): Cursor {
        return database.query(
            DATABASE_TABLE,
            null,
            "$USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null,
            null)
    }

    fun checkIfExist(username: String): Boolean{
        var isExist = true
        val cursor = database.rawQuery("Select * from $DATABASE_TABLE where USERNAME = '$username'",null)
        if(cursor.count <=0){
            isExist = false
        }
        cursor.close()
        return  isExist
    }
    fun insert(values: ContentValues?): Long {
        return database.insert(DATABASE_TABLE, null, values)
    }

    fun deleteById(username: String): Int {
        return database.delete(DATABASE_TABLE, "USERNAME = '$username'", null)
    }
}