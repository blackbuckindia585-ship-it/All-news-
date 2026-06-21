package com.example.data

import kotlinx.coroutines.flow.Flow

class BlogRepository(private val blogDao: BlogDao) {
    val allBlogs: Flow<List<BlogItem>> = blogDao.getAllBlogs()

    suspend fun insert(blog: BlogItem) {
        blogDao.insertBlog(blog)
    }

    suspend fun delete(id: Int) {
        blogDao.deleteBlogById(id)
    }
}
