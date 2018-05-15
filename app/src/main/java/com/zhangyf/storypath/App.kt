package com.zhangyf.storypath

import android.app.Activity
import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import kotlin.properties.Delegates

/**
 * Application
 *
 * @author zhangyf
 * @date 2018/5/15 0015.
 */
class App : Application() {

    companion object {

        private const val TAG = "App"

        var context: Context by Delegates.notNull()
            private set

        var screenWidth:Int = 0
        var screenHeight:Int = 0
    }

    override fun onCreate() {
        super.onCreate()
        screenWidth = this.getScreenWidth()
        screenHeight = this.getScreenHeight()
        context = applicationContext
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    private val mActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.d(TAG, "onCreated: " + activity.componentName.className)
        }

        override fun onActivityStarted(activity: Activity) {
            Log.d(TAG, "onStart: " + activity.componentName.className)
        }

        override fun onActivityResumed(activity: Activity) {

        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            Log.d(TAG, "onDestroy: " + activity.componentName.className)
        }
    }
}
