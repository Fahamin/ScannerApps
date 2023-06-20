package com.scan.imagetotext.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scan.imagetotext.Model.ScanResultModel
import com.scan.imagetotext.R
import com.scan.imagetotext.ResultActivity

class CustomAdapter(val context: Context, private val mList: List<ScanResultModel>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.save_file_item_view, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val model = mList[position]
        holder.imageView.setImageResource(R.drawable.file)
        holder.textView.text = model.fileName

        holder.itemView.setOnClickListener(View.OnClickListener {
            context.startActivity(
                Intent(context, ResultActivity::class.java).putExtra(
                    "result",
                    model.resultData
                )
            )
        })
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}