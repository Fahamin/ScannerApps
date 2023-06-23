package com.scan.imagetotext

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.scan.imagetotext.Model.ScanResultModel
import com.scan.imagetotext.DatabaseHelper.ScanResultDatabaseHelper
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultActivity : AppCompatActivity() {
    lateinit var result: String
    lateinit var resultTv: EditText
    lateinit var db: ScanResultDatabaseHelper

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        resultTv = findViewById(R.id.tvResult)
        db = ScanResultDatabaseHelper(this)

        result = intent.getStringExtra("result")!!
        resultTv.setText(result)

        findViewById<View>(R.id.editID).setOnClickListener(View.OnClickListener {
            resultTv.setSelection(0)

        })
        findViewById<View>(R.id.copyID).setOnClickListener(View.OnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("copy", resultTv.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copy", Toast.LENGTH_SHORT).show()

        })
        findViewById<View>(R.id.saveID).setOnClickListener(View.OnClickListener {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = Date()
            val timeStamp = dateFormat.format(date).toString()

            val data = resultTv.text.toString()
            val model = ScanResultModel(0, data, timeStamp)
            db.insertToScanResult(model)

            startActivity(Intent(this, FileSaveActivity::class.java));
        })

        findViewById<View>(R.id.shareID).setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody = resultTv.text.toString()
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "ScanResult")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(intent, "ScanResult"))

        })
    }

    fun commonDocumentDirPath(FolderName: String): File? {
        var dir: File? = null
        dir = if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
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

   /* private fun getneratePdf() {
        PdfGenerator.getBuilder().setContext(this).fromViewSource().fromView(resultTv)
            .setFileName("Test-PDF").setFolderName("Test-PDF-folder").openPDFafterGeneration(true)
            .build(object : PdfGeneratorListener() {
                override fun onFailure(failureResponse: FailureResponse) {
                    super.onFailure(failureResponse)
                }

                override fun showLog(log: String) {
                    super.showLog(log)
                }

                override fun onStartPDFGeneration() {*//*When PDF generation begins to start*//*
                }

                override fun onFinishPDFGeneration() {*//*When PDF generation is finished*//*
                }

                override fun onSuccess(response: SuccessResponse) {
                    super.onSuccess(response)
                }
            })
    }

*/
    private fun writeToFile(data: String) {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val now = Date()
        val fileName = formatter.format(now) + ".tar.gz"
        val path = "/sdcard/$fileName"

        val fos: FileOutputStream
        try {
            fos = FileOutputStream(path)
            //default mode is PRIVATE, can be APPEND etc.
            fos.write(data.toByteArray())
            fos.close()
            Toast.makeText(
                applicationContext, "filename" + " saved", Toast.LENGTH_LONG
            ).show()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}