package com.battle4play.app

import android.app.Application

class Battle4PlayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("Battle4Play", "Uncaught exception on thread ${thread.name}", throwable)
            throw throwable
        }
    }
}
