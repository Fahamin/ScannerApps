package com.scan.imagetotext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;

public class ScanPdfActivity extends AppCompatActivity {

    String filePath = "";
    String strFileName;
    public String[] itemALL;

    TextView nametv;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_pdf);
        setTitle("Pdf To Text");

        nametv = findViewById(R.id.nameTv);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        pdfLauncher.launch(intent);

    }

    ActivityResultLauncher<Intent> pdfLauncher = registerForActivityResult(new ActivityResultContracts.
            StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @SuppressLint("Range")
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode() == Activity.RESULT_OK) {
                // There are no request codes
                Intent data = result.getData();
                // Get the d of the selected file
                uri = data.getData();
                String uriString = uri.toString();
                File myFile = new File(uriString);
                String[] pathArr = myFile.getAbsolutePath().split(":/");

                String displayName = null;
                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            nametv.setText(displayName);
                        }
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.getName();
                }
            }

        }
    });

    public static String getPath(Context context, Uri uri) {
        // Check if the URI is a document
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // Get the document ID
            String docId = DocumentsContract.getDocumentId(uri);

            // Split the document ID
            String split = docId.split(":")[1];

            // Get the real path
            String path = DocumentsContract.buildDocumentUri(uri.getAuthority(), split).getPath();
            Log.e("pdffound", path);

            return path;
        } else {
            // The URI is not a document
            return uri.getPath();
        }
    }

    private void extractPDF() {
        try {
            // creating a string for
            // storing our extracted text.
            String extractedText = "";
            // creating a variable for pdf reader
            // and passing our PDF file in it.
            PdfReader reader = new PdfReader(getPath(this,uri));

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

    public void ScanNow(View view)  {
        extractPDF();
        //Log.e("pdffound", text);

    }


}