package com.scan.imagetotext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scan.imagetotext.Adapter.CustomAdapter
import com.scan.imagetotext.Interface_All.ItemClickListener
import com.scan.imagetotext.Model.ScanResultModel
import com.scan.imagetotext.DatabaseHelper.ScanResultDatabaseHelper

class FileSaveActivity : AppCompatActivity(),
    ItemClickListener {

    lateinit var db: ScanResultDatabaseHelper
    lateinit var data: MutableList<ScanResultModel>
    lateinit var adapter: CustomAdapter
    lateinit var recyclerview: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_save_activitu)

        db = ScanResultDatabaseHelper(this)
        data = ArrayList<ScanResultModel>()
        recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        data = db.allScanResult as MutableList<ScanResultModel>
        adapter = CustomAdapter(this, data, this)
        recyclerview.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun clickListener(pos: Int) {
        db.deleteScanResultById(pos)
        data = db.allScanResult as MutableList<ScanResultModel>
        adapter = CustomAdapter(this, data, this)
        recyclerview.adapter = adapter
        adapter.notifyDataSetChanged()

    }
}