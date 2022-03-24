package io.myfirst.library.callbacks

interface RemoteListenerCallback {

    fun onFalseCode(int: Int)

    fun onSuccessCode(offerUrl: String)

    fun onStatusTrue()

    fun onStatusFalse()

    fun nonFirstLaunch(url: String)

    fun onDeepLinkSuccess(offerUrl: String, deep: String)
}