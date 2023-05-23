package com.vaani

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.vaani.data.Files
import com.vaani.db.DB
import com.vaani.player.Player
import com.vaani.ui.home.HomePagerFragment
import com.vaani.util.PermissionUtil
import com.vaani.util.PreferenceUtil

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
        super.onCreate(savedInstanceState)
        instance = this

        PermissionUtil.managePermissions()

        DB.init(applicationContext)
        Files.init()
        Player.init(this)
        PreferenceUtil.init(applicationContext)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container_view, HomePagerFragment())
            }
        }
    }

}

