package com.scan.imagetotext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scan.imagetotext.Adapter.CustomAdapter
import com.scan.imagetotext.Model.ScanResultModel
import com.scan.imagetotext.ResultDatabaseHelper.ScanResultDatabaseHelper

class FileSaveActivity : AppCompatActivity() {

    lateinit var db: ScanResultDatabaseHelper
    lateinit var data: List<ScanResultModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_save_activitu)

        db = ScanResultDatabaseHelper(this)
        data = ArrayList<ScanResultModel>()
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        data = db.allScanResult
        val adapter = CustomAdapter(this, data)
        recyclerview.adapter = adapter
    }
}