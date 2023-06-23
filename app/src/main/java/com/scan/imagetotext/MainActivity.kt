package com.scan.imagetotext

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        findViewById<LinearLayout>(R.id.scanImage).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ScanImageActivity::class.java))

        })

        findViewById<LinearLayout>(R.id.scanQR).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java))

        })

        findViewById<LinearLayout>(R.id.scanBar).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java))

        })

        findViewById<LinearLayout>(R.id.scanPDF).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ScanPdfActivity::class.java))

        })

        findViewById<LinearLayout>(R.id.scanimgetopdf).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ImageToPDfMultiple::class.java))

        })
    }
}