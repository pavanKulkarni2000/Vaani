package com.vaani.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.vaani.R

class HomePagerFragment : Fragment(R.layout.home_pager_layout) {
    private lateinit var homePagerAdapter: HomePagerAdapter
    private lateinit var viewPager: ViewPager2
    private val tabNames = arrayOf("folders with media", "favourite")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homePagerAdapter = HomePagerAdapter(this)
        viewPager = view.findViewById(R.id.home_pager)
        viewPager.adapter = homePagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.home_tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabNames[position]
        }.attach()
    }
}