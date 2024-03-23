package com.vaani.ui

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.db.DB
import com.vaani.player.PlayerUtil
import com.vaani.ui.home.HomePagerFragment
import com.vaani.util.PermissionUtil
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG

@UnstableApi
class MainActivity : AppCompatActivity(R.layout.main_activity) {

  companion object {
    private lateinit var instance: FragmentActivity

    val context: Context
      get() = instance

    val fragmentActivity: FragmentActivity
      get() = instance

    val contentResolver: ContentResolver
      get() = instance.contentResolver

    val menuGroupActiveMap = mutableMapOf(R.id.sort_rank_group to false)

    //    lateinit var menuItemAction: MenuItemAction
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    instance = this
    PlayerUtil.init(this)
    PermissionUtil.managePermissions(this)
    DB.init(this)
    PreferenceUtil.init(this)
    if (supportFragmentManager.findFragmentById(R.id.fragment_container_view) == null) {
      supportFragmentManager.commit {
        setReorderingAllowed(true)
        add(R.id.fragment_container_view, HomePagerFragment())
      }
    }
    //    menuItemAction = FolderFragment
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menu?.let {
      it.clear()
      menuInflater.inflate(R.menu.home_activity_menu, it)
      //      FolderFragment.createMenu(menu)
    }
    Log.d(TAG, "onCreateOptionsMenu: create called")
    return super.onCreateOptionsMenu(menu)
  }

  override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
    menu?.let {
      menuGroupActiveMap.forEach { entry ->
        it.setGroupEnabled(entry.key, entry.value)
        it.setGroupVisible(entry.key, entry.value)
      }
    }
    Log.d(TAG, "onPrepareOptionsMenu: prepare called")
    return super.onPrepareOptionsMenu(menu)
  }

  //  override fun onOptionsItemSelected(item: MenuItem): Boolean {
  //    return menuItemAction.itemAction(item) || super.onOptionsItemSelected(item)
  //  }

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
