package com.battle4play.app

import android.app.Application
import android.util.Log

class Battle4PlayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("Battle4Play", "Uncaught exception on thread ${thread.name}", throwable)
            throw throwable
        }
    }
}
