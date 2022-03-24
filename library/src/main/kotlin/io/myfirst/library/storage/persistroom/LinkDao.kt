package io.myfirst.library.storage.persistroom

import androidx.lifecycle.LiveData
import androidx.room.*
import io.myfirst.library.storage.persistroom.model.Link

@Dao
interface LinkDao {
    @Query("SELECT * FROM test")
    fun getAll(): LiveData<List<Link>>

    @Query("SELECT * FROM test")
    fun getAllData(): List<Link>

    @Update
    fun updateLink(link: Link)

    @Insert
    fun addLink(link: Link)

    @Delete
    fun delete(link: Link)
}