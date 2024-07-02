package com.correct.correctsoc.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("select * from UserData")
    suspend fun getUser(): User?

    @Query("update UserData set password = :newPassword where id = :id")
    suspend fun updatePassword(id: String, newPassword: String)

    @Query("select id from UserData")
    suspend fun getUserID(): String?

    @Query("select token from UserData")
    suspend fun getUserToken(): String?

    @Query("select phone from UserData")
    suspend fun getUserPhone(): String?
}