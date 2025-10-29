package com.vaani.ui.util

import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchView
import com.vaani.dal.Files
import com.vaani.model.Search
import com.vaani.ui.adapter.Adapter
import com.vaani.ui.adapter.ItemClickProvider

object GlobalMediaSearcher : ItemClickProvider {
    val displayList = mutableListOf<Search>()
    val adapter = Adapter(displayList,this)

    fun setUp(searchView: SearchView,recyclerView: RecyclerView){
        searchView.addTransitionListener { _: SearchView?, previousState: SearchView.TransitionState?, newState: SearchView.TransitionState ->
            if (newState == SearchView.TransitionState.SHOWING) {
                recyclerView.adapter = adapter
                start()
            }
        }
        searchView.editText.setOnEditorActionListener(
            object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    v?.text?.let {
                        if (it.isNotEmpty()){
                            searchPrediction(it.toString())
                            return true
                        }
                    }
                    return false
                }
            }
        )
        searchView.addTransitionListener { _: SearchView?, previousState: SearchView.TransitionState?, newState: SearchView.TransitionState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                close()
            }
        }
    }
    fun start(){
        displayList.clear()
    }
    fun searchPrediction(query:String){
        search(query)
    }
    fun searchSubmit(query:String){
        search(query)
    }
    private fun search(query:String) {
        displayList.clear()
        val folders = Files.searchFolders(query)
        displayList.addAll(folders.sortedBy { it.name.lowercase() })
        adapter.notifyDataSetChanged()
    }

    fun close(){
        displayList.clear()
    }

    override fun onItemClick(position: Int, view: View?) {
        // TODO
    }

    override fun onItemLongClick(position: Int, view: View?): Boolean {
        // TODO
        return false
    }
}