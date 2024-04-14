package com.vaani

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import com.vaani.db.DB
import com.vaani.player.PlayerUtil
import com.vaani.ui.fragments.HomeFragment
import com.vaani.util.PermissionUtil
import com.vaani.util.PreferenceUtil

@UnstableApi
class MainActivity : AppCompatActivity(R.layout.activity_main) {

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
    PreferenceUtil.init(this)
    supportFragmentManager.commit {
      setReorderingAllowed(true)
      add(R.id.main_activity_fragment_container_view, HomeFragment())
    }
    //    menuItemAction = FolderFragment
  }

  //  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
  //    menu?.let { menuInflater.inflate(R.menu.home_activity_menu, it) }
  //    return super.onCreateOptionsMenu(menu)
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
