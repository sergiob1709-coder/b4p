package com.battle4play.app

import android.app.Application
import com.google.android.gms.ads.MobileAds

class Battle4PlayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("Battle4Play", "Uncaught exception on thread ${thread.name}", throwable)
            throw throwable
        }
    }
}
