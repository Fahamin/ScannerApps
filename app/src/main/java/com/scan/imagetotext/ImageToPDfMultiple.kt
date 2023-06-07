package com.scan.imagetotext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.scan.imagetotext.Utils.ViewPagerAdapter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ImageToPDfMultiple : AppCompatActivity() {
    lateinit var select: Button
    lateinit var previous: Button
    lateinit var next: Button

    var PICK_IMAGE_MULTIPLE = 1
    lateinit var imageEncoded: String
    lateinit var total: TextView
    lateinit var mArrayUri: ArrayList<Uri>
    var position = 0
    lateinit var imagesEncodedList: List<String>
    lateinit var mViewPager: ViewPager
    lateinit var mViewPagerAdapter: ViewPagerAdapter
    lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_to_pdf_multiple)

        select = findViewById(R.id.select)
        mArrayUri = java.util.ArrayList()
        mViewPager = findViewById<View>(R.id.viewPagerMain) as ViewPager

        findViewById<View>(R.id.btn_convert).setOnClickListener {
            createPdf()
        }

        select.setOnClickListener { // initialising intent
            val intent = Intent()
            // setting type to select to be image
            intent.type = "image/*"
            // allowing multiple image to be selected
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            imagePickerLuncher.launch(Intent.createChooser(intent, "Select Picture"))
        }

        mViewPager.setOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        mArrayUri[mViewPager.currentItem]
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }
    fun createPdf() {
        if (methodRequiresTwoPermission()) {
            createPDF()
        }
    }
    private fun methodRequiresTwoPermission(): Boolean {
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(
                this,
                perms[0]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            launcherPermission.launch(perms);
            return false
        }
    }

    var launcherPermission = registerForActivityResult<Array<String>, Map<String, Boolean>>(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted: Map<String, Boolean> ->
        for (s in granted.keys) {
            Log.e("MultiplePermission", "key$s")
            //    Log.e("MultiplePermission","key"+s);
        }
        for (s in granted.values) {
            Log.e("MultiplePermission", "key$s")
        }

    }


    var imagePickerLuncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // There are no request codes
            val data = result.data
            if (data!!.clipData != null) {
                val mClipData = data.clipData
                val cout = data.clipData!!.itemCount
                for (i in 0 until cout) {
                    // adding imageuri in array
                    val imageurl = data.clipData!!.getItemAt(i).uri
                    mArrayUri.add(imageurl)
                    // Initializing the ViewPagerAdapter
                    mViewPagerAdapter = ViewPagerAdapter(this@ImageToPDfMultiple, mArrayUri)
                    // Adding the Adapter to the ViewPager
                    mViewPager.adapter = mViewPagerAdapter

                    // creating a variable for our image processor.

                    // initializing bitmap with our image resource.
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                            contentResolver,
                            mArrayUri[mViewPager.currentItem]
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    //  initImageSetup();
                }
                // setting 1st selected image into image switcher
                position = 0
            } else {
                val imageurl = data.data
                mArrayUri.add(imageurl!!)
                mViewPagerAdapter = ViewPagerAdapter(this@ImageToPDfMultiple, mArrayUri)
                // Adding the Adapter to the ViewPager
                mViewPager.adapter = mViewPagerAdapter
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        mArrayUri[mViewPager.currentItem]
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                // initImageSetup();
                position = 0
            }
        } else {
            // show this if no image is selected
            Toast.makeText(
                this@ImageToPDfMultiple,
                "You haven't picked Image",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun createPDF() {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val now = Date()
        val fileName = "/" + formatter.format(now) + ".pdf"

        //  final File file = new File("/sdcard/test.pdf");
        val file = File(commonDocumentDirPath("pdf").toString())
        Thread {
            var bitmap: Bitmap? = null
            val document = PdfDocument()
            //  int height = 842;
            //int width = 595;
            var height = 1010
            var width = 714
            var reqH: Int
            var reqW: Int
            reqW = width
            for (i in mArrayUri.indices) {
                Log.e("path", mArrayUri[i].path!!)
                try {
                    bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, mArrayUri[i])
                    height = bitmap.height
                    width = bitmap.width
                    reqW = width
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                reqH = width * bitmap!!.height / bitmap.width
                Log.e("reqH", "=$reqH")
                if (reqH < height) {
                    //  bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
                } else {
                    reqH = height
                    reqW = height * bitmap.width / bitmap.height
                    Log.e("reqW", "=$reqW")
                    //   bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
                }
                val pageInfo = PageInfo.Builder(reqW, reqH, 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                Log.e("PDF", "pdf = " + bitmap.width + "x" + bitmap.height)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)
            }
            val fos: FileOutputStream
            try {
                fos = FileOutputStream(file.toString() + fileName)
                document.writeTo(fos)
                document.close()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            runOnUiThread {
                Log.e("path", "ok done")
                Toast.makeText(this, "successful pdf create", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.start()
    }

    fun commonDocumentDirPath(FolderName: String): File? {
        var dir: File? = null
        dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/" + FolderName
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + FolderName)
        }

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            val success = dir.mkdirs()
            if (!success) {
                dir = null
            }
        }
        return dir
    }
}