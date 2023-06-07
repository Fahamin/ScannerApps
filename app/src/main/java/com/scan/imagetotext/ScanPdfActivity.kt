package com.scan.imagetotext

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Constraints
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.scan.imagetotext.Utils.FileDialog
import java.io.File

class ScanPdfActivity : AppCompatActivity() {

    var filePath = ""
    lateinit var strFileName: String
    lateinit var nametv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_pdf)
        nametv = findViewById(R.id.nameTv)
        getFileFromStorage()
    }

    /* final ArrayList<File> allsong = readfile(Environment.getExternalStorageDirectory());

    public ArrayList<File> readfile(File file) {
        ArrayList<File> list = new ArrayList<>();

        File[] allfiles = file.listFiles();

        if (allfiles != null) {
            for (File singleFile : allfiles) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    list.addAll(readfile(singleFile));
                } else {
                    if (singleFile.getName().endsWith(".m3u")) {
                        list.add(singleFile);
                    }
                }
            }
        }

        return list;
    }
*/
    private fun extractPDF() {
        try {
            // creating a string for
            // storing our extracted text.
            var extractedText = ""
            // creating a variable for pdf reader
            // and passing our PDF file in it.
            val reader = PdfReader(filePath)

            // below line is for getting number
            // of pages of PDF file.
            val n = reader.numberOfPages

            // running a for loop to get the data from PDF
            // we are storing that data inside our string.
            for (i in 0 until n) {
                extractedText = """
                $extractedText${
                    PdfTextExtractor.getTextFromPage(reader, i + 1).trim { it <= ' ' }
                }
                
                """.trimIndent()
                // to extract the PDF content from the different pages
            }

            // after extracting all the data we are
            // setting that string value to our text view.
            startActivity(
                Intent(this, ResultActivity::class.java).putExtra(
                    "result",
                    extractedText
                )
            )
            // below line is used for closing reader.
            reader.close()
        } catch (e: Exception) {
            // for handling error while extracting the text file.
            //  extractedTV.setText("Error found is : \n" + e);
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFileFromStorage(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            ) == 0
        ) {
            val fileDialog2 = FileDialog(this, Environment.getExternalStorageDirectory())
            fileDialog2.setFileEndsWith(".pdf")
            fileDialog2.addFileListener(object : FileDialog.FileSelectedListener {
                override fun fileSelected(file: File) {
                    filePath = file.absolutePath
                    println(file.absolutePath)
                    val filetwo = File(filePath)
                    strFileName = filetwo.name
                    nametv.text = strFileName
                    Log.e("name", strFileName)
                    val printStream = System.out
                    printStream.println("selected file :$file")
                }
            })
            fileDialog2.showDialog()
            Log.v(Constraints.TAG, "Permission is granted")
            true
        } else {
            Log.v(Constraints.TAG, "Permission is revoked")
            ActivityCompat.requestPermissions(
                this,
                arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"),
                1
            )
            false
        }
    }

    fun ScanNow(view: View) {
        if (filePath.isEmpty()) {
            Toast.makeText(this, "Pdf File Not Selected", Toast.LENGTH_SHORT).show()
        } else {
            extractPDF()
        }
    }
}