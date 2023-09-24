package com.vaani

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import com.vaani.data.Files
import com.vaani.db.DB
import com.vaani.player.PlayerUtil
import com.vaani.ui.home.HomePagerFragment
import com.vaani.util.PermissionUtil
import com.vaani.util.PreferenceUtil

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

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        PlayerUtil.init(this)
        PermissionUtil.managePermissions(this)
        DB.init(this)
        Files.init()
        PreferenceUtil.init(this)
        if (supportFragmentManager.findFragmentById(R.id.fragment_container_view) == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container_view, HomePagerFragment::class.java, Bundle.EMPTY)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.file_list_action_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        DB.resume(this)
        PlayerUtil.resume(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DB.close()
        PreferenceUtil.close()
        PlayerUtil.close()
    }
}

