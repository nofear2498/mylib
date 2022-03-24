package io.myfirst.library

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import io.myfirst.library.Constants.appsDevKey
import io.myfirst.library.managers.AppsflyerManager
import io.myfirst.library.managers.FirebaseRemoteListener
import io.myfirst.library.managers.OneSignalManager
import io.myfirst.library.storage.Repository
import io.myfirst.library.storage.persistroom.LinkDatabase
import io.myfirst.library.storage.prefs.StorageUtils

object AppsProjector {

    var data: MutableMap<String, Any>? = mutableMapOf()
    var deepLink: String = "null"
    lateinit var preferences: StorageUtils.Preferences
    var repository: Repository? = null



    fun createRemoteConfigInstance(activity: Activity): FirebaseRemoteListener {
        preferences = StorageUtils.Preferences(activity, Constants.NAME,
            Constants.MAINKEY,
            Constants.CHYPRBOOL
        )
        return FirebaseRemoteListener(activity)
    }

    fun createAppsInstance(context: Context, devKey: String): AppsflyerManager {
        appsDevKey = devKey
       return AppsflyerManager(context as AppCompatActivity, devKey)
    }

    fun createOneSignalInstance(context: Context, oneSignalId: String): OneSignalManager {
      /*  val userDao = LinkDatabase.getDatabase(context).linkDao()
        repository = Repository(userDao)*/
        return OneSignalManager(context, oneSignalId)
    }

    fun createRepoInstance(context: Context): Repository {
        if (repository == null){
            return Repository(LinkDatabase.getDatabase(context).linkDao())
        } else {
            return repository as Repository
        }
    }

    //class AppsProjector
}