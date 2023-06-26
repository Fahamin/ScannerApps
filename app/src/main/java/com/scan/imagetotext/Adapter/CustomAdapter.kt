package com.scan.imagetotext.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scan.imagetotext.Interface_All.ItemClickListener
import com.scan.imagetotext.Model.ScanResultModel
import com.scan.imagetotext.R
import com.scan.imagetotext.ResultActivity

class CustomAdapter(
    val context: Context,
    var mList: MutableList<ScanResultModel>,
    var itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    //   var db: ScanResultDatabaseHelper = ScanResultDatabaseHelper(context)

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

        holder.optionMenu.setOnClickListener(View.OnClickListener {
            showPopup(holder.optionMenu, model, context, itemClickListener);

        })
    }

    private fun showPopup(
        view: View,
        amodel: ScanResultModel,
        mcontext: Context,
        itemClickListener: ItemClickListener
    ) {
        val popupMenu = PopupMenu(context, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.popmenu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(
            MyMenuClickListener(
                amodel,
                mcontext,
                mList,
                itemClickListener
            )
        )

        popupMenu.show()
    }

    private class MyMenuClickListener(
        amodel: ScanResultModel,
        context: Context,
        mList: MutableList<ScanResultModel>,
        itemClickListener: ItemClickListener
    ) :
        PopupMenu.OnMenuItemClickListener {
        var amodel: ScanResultModel
        var context: Context
        var mList: MutableList<ScanResultModel>
        var itemClickListener: ItemClickListener

        init {
            this.amodel = amodel
            this.context = context
            this.mList = mList
            this.itemClickListener = itemClickListener

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            if (item.itemId == R.id.pop_deletID) {
                itemClickListener.clickListener(amodel.id)
            }
            if (item.itemId == R.id.pop_openID) {
                context.startActivity(
                    Intent(context, ResultActivity::class.java).putExtra(
                        "result",
                        amodel.resultData
                    )
                )
            }
            return false
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val optionMenu: ImageView = itemView.findViewById(R.id.optionID)
        val textView: TextView = itemView.findViewById(R.id.textID)
    }


}