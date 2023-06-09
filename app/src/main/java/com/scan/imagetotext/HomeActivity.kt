package com.scan.imagetotext

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.scan.imagetotext.Utils.Fun.Companion.appurl
import com.scan.imagetotext.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawer: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHome.toolbar)
        drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            binding.appBarHome.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        FirebaseApp.initializeApp(this)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.bringToFront()

        navigationView.setNavigationItemSelectedListener { item ->

            if (item.itemId == R.id.nav_ImageToPdf) {
                startActivity(Intent(this, ImageToPDfMultiple::class.java))
            }

            if (item.itemId == R.id.nav_SaveFile) {
                startActivity(Intent(this, FileSaveActivity::class.java))
            }

            if (item.itemId == R.id.nav_ScanImage) {
                startActivity(Intent(this, ScanImageActivity::class.java))

            }
            if (item.itemId == R.id.nav_ScanBAR) {
                startActivity(Intent(this, QrCodeActivity::class.java))
            }

            if (item.itemId == R.id.nav_ScanQR) {
                startActivity(Intent(this, QrCodeActivity::class.java))
            }
            if (item.itemId == R.id.nav_share) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Install now")
                val app_url: String = appurl
                shareIntent.putExtra(Intent.EXTRA_TEXT, app_url)
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }

            if (item.itemId == R.id.nav_send) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("abcayesha28@gmail.com"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "Write Subject")
                intent.putExtra(Intent.EXTRA_TEXT, "Type message here")
                intent.type = "email/rfc822"
                startActivity(Intent.createChooser(intent, "Select Email for message :"))
            }
            drawer.closeDrawer(GravityCompat.START)
            false
        }

        findViewById<LinearLayout>(R.id.scanImage).setOnClickListener {
            startActivity(Intent(this, ScanImageActivity::class.java))

        }

        findViewById<LinearLayout>(R.id.scanQR).setOnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java))

        }

        findViewById<LinearLayout>(R.id.scanBar).setOnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java))

        }

        findViewById<LinearLayout>(R.id.scanPDF).setOnClickListener {
            startActivity(Intent(this, ScanPdfActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.scanimgetopdf).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ImageToPDfMultiple::class.java))

        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

}