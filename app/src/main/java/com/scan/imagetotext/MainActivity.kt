package com.scan.imagetotext

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<LinearLayout>(R.id.scanImage).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ScanImageActivity::class.java))

        })

        findViewById<LinearLayout>(R.id.scanImage).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java))

        })

        findViewById<LinearLayout>(R.id.scanImage).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java))

        })

        findViewById<LinearLayout>(R.id.scanImage).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ScanPdfActivity::class.java))

        })
        findViewById<LinearLayout>(R.id.scanImage).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ImageToPDfMultiple::class.java))

        })
    }
}