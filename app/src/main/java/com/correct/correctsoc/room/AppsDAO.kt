package com.correct.correctsoc.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: App)

    @Query("delete from AppLockData where packageName = :packageName")
    suspend fun unLockApp(packageName: String)

    @Query("update AppLockData set isAllowed = 1 where packageName = :packageName")
    suspend fun allowForApp(packageName: String)

    // call every 1 minutes
    @Query("update AppLockData set isAllowed = 0 where 1")
    suspend fun lockAppsAfterOpen()

    @Query("select * from AppLockData")
    suspend fun getLockedApp(): List<App>?

    @Query("select packageName from AppLockData")
    suspend fun getPackages(): List<String>?

    @Query("select * from AppLockData where packageName = :packageName")
    suspend fun getAppData(packageName: String): App?
}