package com.vaani

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import com.vaani.player.PlayerUtil
import com.vaani.ui.fragments.HomeFragment
import com.vaani.util.PermissionUtil
import com.vaani.util.PreferenceUtil

@UnstableApi
class MainActivity : AppCompatActivity(R.layout.activity_main) {

  private lateinit var playerUtil: PlayerUtil

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
    playerUtil = (application as VaaniApplication).container.playerUtil
    playerUtil.init()
    PermissionUtil.managePermissions(this)
    PreferenceUtil.init(this)
    supportFragmentManager.commit {
      setReorderingAllowed(true)
      add(R.id.main_activity_fragment_container_view, HomeFragment())
    }
  }

  override fun onResume() {
    super.onResume()
    playerUtil.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    PreferenceUtil.close()
    playerUtil.close()
  }
}
