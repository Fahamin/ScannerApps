package com.scan.imagetotext;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.scan.imagetotext.Utils.FileDialog;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;


public class ScanPdfActivity extends AppCompatActivity {

    String filePath = "";
    String strFileName;

    TextView nametv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_pdf);
        setTitle("Pdf To Text");

        nametv = findViewById(R.id.nameTv);
        getFileFromStorage();
    }

    final ArrayList<File> allFile = readfile(Environment.getExternalStorageDirectory());
    public ArrayList<File> readfile(File file) {
        ArrayList<File> list = new ArrayList<>();

        File[] allfiles = file.listFiles();

        if (allfiles != null) {
            for (File singleFile : allfiles) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    list.addAll(readfile(singleFile));
                } else {
                    if (singleFile.getName().endsWith(".pdf")) {
                        list.add(singleFile);
                    }
                }
            }
        }
        return list;
    }


    private void extractPDF() {
        try {
            // creating a string for
            // storing our extracted text.
            String extractedText = "";
            // creating a variable for pdf reader
            // and passing our PDF file in it.
            PdfReader reader = new PdfReader(filePath);

            // below line is for getting number
            // of pages of PDF file.
            int n = reader.getNumberOfPages();

            // running a for loop to get the data from PDF
            // we are storing that data inside our string.
            for (int i = 0; i < n; i++) {
                extractedText = extractedText + PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + "\n";
                // to extract the PDF content from the different pages
            }

            // after extracting all the data we are
            // setting that string value to our text view.
            startActivity(new Intent(this, ResultActivity.class).putExtra("result", extractedText));
            // below line is used for closing reader.
            reader.close();
        } catch (Exception e) {
            // for handling error while extracting the text file.
            //  extractedTV.setText("Error found is : \n" + e);
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean getFileFromStorage() {
        if (Build.VERSION.SDK_INT < 23) {
            Log.v(Constraints.TAG, "Permission is granted");
            FileDialog fileDialog = new FileDialog(this, Environment.getExternalStorageDirectory());
            fileDialog.setFileEndsWith(".pdf");
            fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    filePath = file.getAbsolutePath();
                    File filetwo = new File(filePath);
                    strFileName = filetwo.getName();
                    nametv.setText(strFileName);
                    Log.e("name", strFileName);
                    PrintStream printStream = System.out;
                }
            });
            fileDialog.showDialog();
            return true;
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            FileDialog fileDialog2 = new FileDialog(this, Environment.getExternalStorageDirectory());
            fileDialog2.setFileEndsWith(".pdf");
            fileDialog2.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    filePath = file.getAbsolutePath();
                    System.out.println(file.getAbsolutePath());
                    File filetwo = new File(filePath);
                    strFileName = filetwo.getName();
                    nametv.setText(strFileName);

                    Log.e("name", strFileName);
                    PrintStream printStream = System.out;
                    printStream.println("selected file :" + file.toString());
                }
            });
            fileDialog2.showDialog();
            Log.v(Constraints.TAG, "Permission is granted");
            return true;
        } else {
            Log.v(Constraints.TAG, "Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            return false;
        }
    }

    public void ScanNow(View view) {

        if (filePath.isEmpty()) {
            Toast.makeText(this, "Pdf File Not Selected", Toast.LENGTH_SHORT).show();
        } else {
            extractPDF();
        }
    }


}