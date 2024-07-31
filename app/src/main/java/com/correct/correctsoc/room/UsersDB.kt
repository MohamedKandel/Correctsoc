package com.correct.correctsoc.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, App::class], version = 1)
abstract class UsersDB : RoomDatabase() {
    abstract fun dao(): DAO
    abstract fun appDao(): AppsDAO

    companion object {
        @Volatile
        private var INSTANCE: UsersDB? = null
        fun getDBInstance(context: Context): UsersDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UsersDB::class.java,
                    "userDB"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}