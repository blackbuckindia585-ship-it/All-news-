package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlogDao {
    @Query("SELECT * FROM blogs ORDER BY timestamp DESC")
    fun getAllBlogs(): Flow<List<BlogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlog(blog: BlogItem)

    @Query("DELETE FROM blogs WHERE id = :id")
    suspend fun deleteBlogById(id: Int)
}
