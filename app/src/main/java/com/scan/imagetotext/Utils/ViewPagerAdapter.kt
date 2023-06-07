package com.scan.imagetotext.Utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.scan.imagetotext.R
import java.util.Objects

class ViewPagerAdapter constructor(
    context: Context,
    images: ArrayList<Uri>,
) : PagerAdapter() {

    lateinit var images: ArrayList<Uri>
    lateinit var mLayoutInflater: LayoutInflater


    override fun getCount(): Int {

        // return the number of images
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {


        val itemView: View = mLayoutInflater.inflate(R.layout.item, container, false)
        val imageView = itemView.findViewById<View>(R.id.imageViewMain) as ImageView
        imageView.setImageURI(images[position])
        Objects.requireNonNull(container).addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}