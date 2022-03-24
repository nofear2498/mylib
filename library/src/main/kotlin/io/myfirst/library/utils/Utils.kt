package io.myfirst.library.utils

import android.content.Context
import android.os.AsyncTask
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import io.myfirst.library.Constants.appsDevKey
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class Utils {

    companion object {

        var ADId: String? = ""

        fun getAdId(context: Context){
            AsyncTask.execute {
                try {
                    val adInfo: AdvertisingIdClient.Info =
                        AdvertisingIdClient.getAdvertisingIdInfo(context)
                    ADId = if (adInfo != null) adInfo.getId() else null
                    // Use the advertising id
                } catch (exception: IOException) {
                    // Error handling if needed
                } catch (exception: GooglePlayServicesRepairableException) {
                } catch (exception: GooglePlayServicesNotAvailableException) {
                }
            }
        }

        fun concatCampaign(naming: String): String {
            return naming.replace("||", "&").replace("()", "=")
        }
        //test1||sub1()test||sub2()test2

        fun getFinalUrl(baseUrl: String, naming: String, context: Context, campaignId: String, mediaSource: String): String {
            return baseUrl + "?" + "app_id=" + getAppBundle(context) +
                    "&afToken=" + appsDevKey +
                    "&afid=" + AppsFlyerLib.getInstance().getAppsFlyerUID(context) +
                    "&sub11=" + campaignId +
                    "&media_source=" + mediaSource +
                    "&advertising_id=" + ADId +
                    "&triger=" + concatCampaign(naming)



        }


        fun getFinalDeepUrl(baseUrl: String, naming: String, context: Context, fbappid: String, fbappsecret: String): String {

            return baseUrl + "?" + "sub12=" + getAppBundle(context) +
                    "&afToken=" + appsDevKey +
                    "&afid=" + AppsFlyerLib.getInstance().getAppsFlyerUID(context) +
                    "&sub11=" + "facebook" +
                    "&fbappid=" + fbappid +
                    "&fbappsecret=" + fbappsecret +
                    "&media_source=" + "facebook" +
                    "&advertising_id=" + ADId +
                    "&triger=" + concatCampaign(naming)



        }

        fun getAppBundle(context: Context): String {
            return context.packageName
        }


        fun getResponseCode(url: String): Int{
            var int = 0
            try {
                //  Log.d(TAG, "opening url")
                val openUrl = URL(url)
                val http: HttpURLConnection = openUrl.openConnection() as HttpURLConnection
                int = http.responseCode

            } catch (exception: IOException){

            }
            //  Log.d(TAG, "$int  - integer before return")
            return int
        }
    }

}