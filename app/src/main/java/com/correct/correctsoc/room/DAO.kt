package com.correct.correctsoc.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("select * from UserData where id= :id")
    suspend fun getUser(id: String): User?

    @Query("update UserData set password = :newPassword where id = :id")
    suspend fun updatePassword(id: String, newPassword: String)

    @Query("select id from UserData")
    suspend fun getUserID(): String?

    @Query("select token from UserData where id= :id")
    suspend fun getUserToken(id: String): String?

    @Query("select phone from UserData where id= :id")
    suspend fun getUserPhone(id: String): String?

    @Query("select mail from Userdata where id= :id")
    suspend fun getUserMail(id: String): String?

    @Query("update UserData set mail = :newMail where id = :id")
    suspend fun updateMail(id: String, newMail: String)

    @Query("update UserData set phone = :newPhone where id = :id")
    suspend fun updatePhone(id: String, newPhone: String)

    @Query("delete from UserData where id = :id")
    suspend fun deleteUser(id: String)

    @Query("select password from UserData where id = :id")
    suspend fun getPassword(id: String): String?

    @Query("update UserData set token= :token where id= :id")
    suspend fun updateToken(token: String, id: String)

    @Query("update UserData set username= :username where id= :id")
    suspend fun updateUsername(username: String, id: String)

    @Query("select username from UserData where id= :id")
    suspend fun getUsername(id: String): String?
}