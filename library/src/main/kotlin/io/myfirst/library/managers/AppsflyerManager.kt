package io.myfirst.library.managers

import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import io.myfirst.library.AppsProjector

import io.myfirst.library.AppsProjector.preferences
import io.myfirst.library.Constants.LOG
import io.myfirst.library.Constants.ONCONVERSION
import io.myfirst.library.Constants.TAG
import io.myfirst.library.Constants.TRUE
import io.myfirst.library.callbacks.AppsflyerListenerCallback
import io.myfirst.library.callbacks.RemoteListenerCallback
import io.myfirst.library.storage.persistroom.model.Link
import io.myfirst.library.utils.Utils
import java.util.*

class AppsflyerManager(private val activity: AppCompatActivity, private val appsDevKey: String):
    RemoteListenerCallback {

    private lateinit var appsflyerListenerCallback: AppsflyerListenerCallback

    fun start(offerUrl: String) {

        appsflyerListenerCallback = activity as AppsflyerListenerCallback

        //  if (LOG) Log.d(TAG, "got apps Data - method invoked")
        val conversionDataListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                data?.let { cvData ->
                        if (LOG) Log.d(TAG, "got apps Data - succes conversion")
                        when (preferences.getOnConversionDataSuccess(ONCONVERSION)) {
                            "null" -> {
                                preferences.setOnConversionDataSuccess(
                                    ONCONVERSION,
                                    TRUE
                                )


                                    /*
                                    if (LOG) Log.d(TAG, "added link to storage - " + MainClass.utils.getFinalUrl(
                                            part1 + part2 + part3, data["campaign"].toString(), context))
                                     */

                                    val url = concatName(data,"null", activity = activity as AppCompatActivity, offerUrl = Firebase.remoteConfig.getString("offer"))


                                    if (LOG) Log.d(TAG, "$url -- final url")
                                    AppsProjector.createRepoInstance(activity).insert(Link(1, url))
                                    //   if (LOG) Log.d(TAG, "added to viewmodel number 1")
                                    appsflyerListenerCallback.onConversionDataSuccess(data, url)

                            }
                            "true" -> {

                            }
                            "false" -> {

                            }
                            else -> {

                            }
                        }


                }
            }

            override fun onConversionDataFail(error: String?) {
                if (LOG) Log.d(TAG, "onConversionDataFail")
                appsflyerListenerCallback.onConversionDataFail(error)
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                data?.map {
                    if (LOG) Log.d(TAG, "onAppOpenAttribution")
                }
            }

            override fun onAttributionFailure(error: String?) {
                if (LOG) Log.d(TAG, "onAttributionFailure")
            }
        }
        //инициализируем SDK AppsFlyer'a
        AppsFlyerLib.getInstance().init(appsDevKey, conversionDataListener, activity)
        AppsFlyerLib.getInstance().start(activity)
    }

    override fun onFalseCode(int: Int) {

    }

    override fun onSuccessCode(offerUrl: String) {
        Log.d(TAG, "onSuccessCode AppsFlyer Class")
        start(offerUrl)
    }

    override fun onStatusTrue() {

    }

    override fun onStatusFalse() {

    }

    override fun nonFirstLaunch(url: String) {

    }

    override fun onDeepLinkSuccess(
        offerUrl: String,
        deep: String
    ) {

    }

    fun concatName(data: MutableMap<String, Any>?, deep: String, offerUrl: String, activity: AppCompatActivity): String {

        val deepContains = !deep.contains("sub")
        val doubleDeepChecker = deep != "null" && deep.contains("sub")
        val doubleCampaignChecker = data?.get("campaign").toString() != "" && data?.get("campaign").toString() != "null" && data?.get("campaign").toString().contains("sub")
        val doubleCChecker = data?.get("c").toString().contains("sub") && data?.get("c").toString() != "null"

        var deepWithoutMyApp = deep.replace("myapp://", "")
        deepWithoutMyApp = deepWithoutMyApp.replace("||", "doubleline").replace("_","underline")
        deepWithoutMyApp = encodeCharacters(deepWithoutMyApp)


        var campaignReplacedSymbols = data?.get("campaign").toString().replace("||", "doubleline").replace("_","underline")
        campaignReplacedSymbols = encodeCharacters(campaignReplacedSymbols)

        var cFieldreplacedsymbols = data?.get("c").toString().replace("||", "doubleline").replace("_","underline")
        cFieldreplacedsymbols = encodeCharacters(cFieldreplacedsymbols)


        Log.d("library", " launchConcat")
        return  offerUrl+  "?" +
                "&gadid=${Utils.ADId}" +
                "&af_id=${AppsFlyerLib.getInstance().getAppsFlyerUID(activity)}" +
                if (doubleDeepChecker) {
                    //if (logging) Log.d("library", replaceSymbols(deep) + " deep name")
                    replaceSymbols(deepWithoutMyApp)
                } else {
                    ""
                } +

                if (doubleCampaignChecker && deepContains) {
                   // if (logging) Log.d("library", replaceSymbols(campaign) + " campaign name")
                    replaceSymbols(campaignReplacedSymbols)
                } else {
                    ""
                } +

                if (doubleCChecker) {
                    //if (logging) Log.d("library", replaceSymbols(c_field) + " c_field name")
                    replaceSymbols(cFieldreplacedsymbols)
                } else {
                    ""
                } +
                if (doubleDeepChecker) {
                   // if (logging) Log.d("library", "$deep - deep campaign setted1")
                   // if (logging) Log.d("library", "deep campaign setted2")
                   // if (logging) Log.d("library", "&app_campaign=$deep")
                    "&app_campaign=${encodeCharacters(deep)}"
                } else {
                    ""
                } +
                if (doubleCChecker) {
                   // if (logging) Log.d("library", "c_field campaign setted")
                    "&app_campaign=${encodeCharacters(data?.get("c").toString())}"
                } else {
                    ""
                } +
                if (doubleCampaignChecker && deepContains) {
                   // if (logging) Log.d("library", "campaign campaign setted")
                    "&app_campaign=${encodeCharacters(data?.get("campaign").toString())}"
                } else {
                    ""
                } +
                "&orig_cost=${data?.get("orig_cost").toString()}" +
                "&adset_id=${data?.get("adset_id").toString()}" +
                "&campaign_id=${data?.get("campaign_id").toString()}" +
                "&source=${data?.get("media_source").toString()}" +
                "&bundle=${activity.applicationContext.packageName}" +
                "&af_siteid=${data?.get("af_siteid").toString()}" +
                "&currency=${data?.get("currency").toString()}" +
                //"&root=${Utils.isRooted()}" +
               // "&adb=${Utils.collectAdbEnabled(activity)}" +
                "&timezone=${TimeZone.getDefault().id}" +
                "&adset=${encodeCharacters(data?.get("adset").toString())}" +
                "&adgroup=${encodeCharacters(data?.get("adgroup").toString())}"



//||sub1_test||sub2_test
    }


    fun encodeCharacters(parameter: String):String{
        return Uri.encode(parameter)
    }

    fun replaceSymbols(campaign: String): String {
        return campaign.replace("doubleline", "&").replace("underline", "=")
    }

}