package com.inlacou.byvapps.maplistlib.adapter

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inlacou.byvapps.maplistlib.R
import com.inlacou.byvapps.maplistlib.general.ViewUtils
import com.inlacou.byvapps.maplistlib.ui.views.ExampleItemView
import com.inlacou.byvapps.maplistlib.ui.views.ExampleItemViewModel

/**
 * Created by inlacou on 26/06/17.
 */
class EnterpriseRvAdapter(private val context: Context,
                          private val itemList: MutableList<ExampleItemViewModel>,
                          private val orientation: Orientation = EnterpriseRvAdapter.Orientation.VERTICAL,
                          private val callbacks: Callbacks) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var lastAdded: Int = 0

    companion object {
        private val DEBUG_TAG = EnterpriseRvAdapter::class.java.simpleName
    }

    init {
        Log.d(DEBUG_TAG, "init")
    }

    override fun getItemViewType(position: Int): Int {
        // Return type
        when(orientation){
            Orientation.VERTICAL -> return 0
            Orientation.HORIZONTAL -> return 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        Log.d(DEBUG_TAG, "onCreateViewHolder")
        when (viewType) {
            0 -> {
                val layoutView = LayoutInflater.from(parent.context).inflate(
                        R.layout.recyclerview_item_example_vertical, parent, false)
                return myViewHolder1(layoutView)
            }
            1 -> {
                val layoutView = LayoutInflater.from(parent.context).inflate(
                        R.layout.recyclerview_item_example_horizontal, parent, false)
                return myViewHolder1(layoutView)
            }
            else -> return null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(DEBUG_TAG, "onBindViewHolder " + position)
        val viewHolder = holder as myViewHolder1
        viewHolder.view.setCallback(object : ExampleItemView.Callbacks {
            override fun onSurfaceClick(item: ExampleItemViewModel) {
                callbacks.onItemClick(item)
            }

            override fun onDelete(item: ExampleItemViewModel) {
                callbacks.onItemDeleted(item)
            }

            override val data: ExampleItemViewModel
                get() = itemList[position]

        })
        viewHolder.view.getData()
        viewHolder.view.populate()
        if(orientation== Orientation.HORIZONTAL){
            ViewUtils.resizeView2(viewHolder.view, (ViewUtils.getScreenWidthPixels(context as AppCompatActivity)*0.9).toInt(), -1)
        }
        if (position > lastAdded) {
            //viewHolder.view.inAnimation()
            lastAdded = position
        }
    }

    inner class myViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val DEBUG_TAG = myViewHolder1::class.java.name
        val view: ExampleItemView = itemView.findViewById<ExampleItemView>(R.id.view)

        init {
            view.initialize(itemView)
        }
    }

    inner class myViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val DEBUG_TAG = myViewHolder2::class.java.name
        val view: ExampleItemView = itemView.findViewById<ExampleItemView>(R.id.view)

        init {
            view.initialize(itemView)
        }
    }

    override fun getItemCount(): Int {
        return this.itemList.size
    }

    interface Callbacks {
        fun onItemClick(item: ExampleItemViewModel)
        fun onItemDeleted(item: ExampleItemViewModel)
    }

    enum class Orientation{
        VERTICAL, HORIZONTAL
    }
}
