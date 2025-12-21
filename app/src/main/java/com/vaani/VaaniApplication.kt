package com.vaani

import android.app.Application

/**
 * The Application class for the app, which holds the AppContainer.
 */
class VaaniApplication : Application() {
    // AppContainer instance used by the rest of classes to obtain dependencies
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
