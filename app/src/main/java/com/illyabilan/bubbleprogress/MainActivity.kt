package com.illyabilan.bubbleprogress

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.beelancrp.bubbleprogressbar.BubbleProgressBar
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pager = findViewById<ViewPager>(R.id.viewPager)
        val progress = findViewById<BubbleProgressBar>(R.id.progress)
        val items = ArrayList<PagerItem>()
        (0..5).forEach {
            val rnd = Random()
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            items.add(PagerItem(color))
        }
        pager.adapter = Adapter(items)
        progress.setViewPager(pager)

    }

    internal class Adapter(val items: MutableList<PagerItem>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = View.inflate(container.context, R.layout.item_page, null)
            val image = view.findViewById<ImageView>(R.id.image)
            image.setColorFilter(items[position].color)
            container.addView(view)
            return view
        }

        override fun isViewFromObject(view: View, oobject: Any): Boolean {
            return view == oobject
        }

        override fun destroyItem(container: ViewGroup, position: Int, _object: Any) {
            container.removeView(_object as View)
        }

        override fun getCount(): Int {
            return items.size
        }
    }

    internal data class PagerItem(val color: Int)
}
