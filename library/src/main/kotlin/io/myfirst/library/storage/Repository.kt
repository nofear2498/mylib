package io.myfirst.library.storage

import androidx.lifecycle.LiveData
import io.myfirst.library.storage.persistroom.LinkDao
import io.myfirst.library.storage.persistroom.model.Link
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Repository(var linkDao: LinkDao) {

    val readAllData: LiveData<List<Link>> = linkDao.getAll()


    fun getAllData(): List<Link>{
        return linkDao.getAllData()
    }

    fun insert(link: Link){
        GlobalScope.launch(Dispatchers.IO){ linkDao.addLink(link) }
    }

    fun updateLink(link: Link){
        GlobalScope.launch(Dispatchers.IO) { linkDao.updateLink(link)  }
    }
}