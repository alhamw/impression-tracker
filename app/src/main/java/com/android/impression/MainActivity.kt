package com.android.impression

import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_browse.view.*

class MainActivity : AppCompatActivity() {
    val trackedData = mutableListOf<TimeTrackHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val products = mutableListOf<Product>()
        for (i in 0..10) {
            products.add(Product(i))
        }

        rv_browse.adapter = BrowseAdapter(products)
        rv_browse.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_browse.smoothScrollBy(0, 5)
        rv_browse.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val lastPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                checkTrackedDataState(firstPosition, lastPosition)

                for (pos in firstPosition..lastPosition) {
                    val view = recyclerView.layoutManager?.findViewByPosition(pos)

                    view?.let {
                        val itemRect = Rect()
                        view.getLocalVisibleRect(itemRect)
                        val visibleHeight = itemRect.height()
                        val height = view.measuredHeight

                        val visibleHeightPercent = visibleHeight.toDouble() / height.toDouble() * 100
                        val position = (recyclerView.layoutManager as LinearLayoutManager).getPosition(view)

                        trackViewHeightAndTime(visibleHeightPercent, position) {
                            Log.d("TAG", "$position")
                        }
                    }
                }
            }
        })
    }

    private fun checkTrackedDataState(firstPosition: Int, lastPosition: Int) {
        trackedData.filter {
            it.position !in firstPosition until lastPosition + 1 && !it.isTracked
        }.forEach {
            it.stop()
        }
    }

    private fun trackViewHeightAndTime(
        visibleHeightPercent: Double,
        pos: Int,
        listener: () -> Unit
    ) {
        when {
            visibleHeightPercent > 60 && !trackedData.any { it.position == pos } -> {
                trackedData.add(TimeTrackHolder(pos) { listener() })
            }
            visibleHeightPercent > 60 -> {
                trackedData.find { it.position == pos && !it.onTracking && !it.isTracked }?.start()
            }
            visibleHeightPercent < 60 -> {
                trackedData.find { it.position == pos && it.onTracking }?.stop()
            }
        }
    }

    data class Product(val position: Int)

    class BrowseAdapter(val products: MutableList<Product>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            return ItemViewHolder(
                LayoutInflater.from(p0.context).inflate(
                    R.layout.item_browse,
                    p0,
                    false
                )
            )
        }

        override fun getItemCount(): Int = products.size

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            val itemViewHolder = p0 as ItemViewHolder
            itemViewHolder.position = p1
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun setPosition(position: Int) {
                itemView.tv_position.text = position.toString()
            }
        }
    }
}
