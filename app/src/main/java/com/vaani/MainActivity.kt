package com.vaani

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.vaani.data.Files
import com.vaani.db.DB
import com.vaani.player.PlayerUtil
import com.vaani.ui.home.HomePagerFragment
import com.vaani.ui.player.PlayerFragment
import com.vaani.util.AppAction
import com.vaani.util.Constants.ACTION
import com.vaani.util.PermissionUtil
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG

@UnstableApi
class MainActivity : AppCompatActivity(R.layout.main_layout) {

    companion object {
        private lateinit var instance: FragmentActivity

        val context: Context
            get() = instance

        val fragmentActivity: FragmentActivity
            get() = instance

        val contentResolver: ContentResolver
            get() = instance.contentResolver

        val application: Application
            get() = instance.application
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: created ${this.hashCode()}")
        super.onCreate(savedInstanceState)
        instance = this

        PlayerUtil.init(applicationContext)
        PermissionUtil.managePermissions()
        DB.init(applicationContext)
        Files.init()
        PreferenceUtil.init(applicationContext)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.fragment_container_view, HomePagerFragment::class.java, Bundle.EMPTY)
            if(intent.extras?.getString(ACTION)?.equals(AppAction.ACTION_START_PLAYER) == true || PlayerUtil.controller?.isPlaying == true){
                add(R.id.fragment_container_view, PlayerFragment::class.java, Bundle.EMPTY)
                addToBackStack(null)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        DB.close()
        PreferenceUtil.close()
        PlayerUtil.close()
    }
}

