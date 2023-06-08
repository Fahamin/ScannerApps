package com.scan.imagetotext;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;


import com.scan.imagetotext.Utils.ViewPagerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ImageToPDfMultiple extends AppCompatActivity {

    Button select, previous, next;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    TextView total;
    ArrayList<Uri> mArrayUri;
    int position = 0;
    List<String> imagesEncodedList;
    // creating object of ViewPager
    ViewPager mViewPager;
    // Creating Object of ViewPagerAdapter
    ViewPagerAdapter mViewPagerAdapter;

    Bitmap bitmap;
    // creating a variable for image view.
    String[] perms =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_pdf_multiple);


        setTitle("Image To PDF");
        select = findViewById(R.id.select);
        mArrayUri = new ArrayList<Uri>();

        // Initializing the ViewPager Object
        mViewPager = (ViewPager) findViewById(R.id.viewPagerMain);

        // click here to select image
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (methodRequiresTwoPermission()) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // setting type to select to be image
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    imagePickerLuncher.launch(Intent.createChooser(intent, "Select Picture"));

                }

            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mArrayUri.get(mViewPager.getCurrentItem()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        findViewById(R.id.btn_convert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPdf();
            }
        });
    }

    private boolean methodRequiresTwoPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                perms[0]
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            launcherPermission.launch(perms);
            return false;
        }

    }

    ActivityResultLauncher<String[]> launcherPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), granted -> {

                if (ContextCompat.checkSelfPermission(this, perms[0]) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, perms[1]) == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                    this.finish();
                }


            });

    // Luncher  imagepick from gallary
    ActivityResultLauncher<Intent> imagePickerLuncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            int cout = data.getClipData().getItemCount();
                            for (int i = 0; i < cout; i++) {
                                // adding imageuri in array
                                Uri imageurl = data.getClipData().getItemAt(i).getUri();
                                mArrayUri.add(imageurl);
                                // Initializing the ViewPagerAdapter
                                mViewPagerAdapter = new ViewPagerAdapter(ImageToPDfMultiple.this, mArrayUri);
                                // Adding the Adapter to the ViewPager
                                mViewPager.setAdapter(mViewPagerAdapter);

                                // creating a variable for our image processor.

                                // initializing bitmap with our image resource.
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mArrayUri.get(mViewPager.getCurrentItem()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //  initImageSetup();
                            }
                            // setting 1st selected image into image switcher
                            position = 0;
                        } else {
                            Uri imageurl = data.getData();
                            mArrayUri.add(imageurl);
                            mViewPagerAdapter = new ViewPagerAdapter(ImageToPDfMultiple.this, mArrayUri);
                            // Adding the Adapter to the ViewPager
                            mViewPager.setAdapter(mViewPagerAdapter);
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mArrayUri.get(mViewPager.getCurrentItem()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // initImageSetup();
                            position = 0;

                        }
                    } else {
                        // show this if no image is selected
                        Toast.makeText(ImageToPDfMultiple.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
                    }
                }
            });


    public void createPdf() {

        if (methodRequiresTwoPermission()) {
            createPDF();
        }

    }

    private void createPDF() {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Creating PDF...");
        dialog.show();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        String fileName = "/" + formatter.format(now) + ".pdf";

        //  final File file = new File("/sdcard/test.pdf");
        final File file = new File(String.valueOf(commonDocumentDirPath()));

        new Thread(() -> {
            Bitmap bitmap = null;
            PdfDocument document = new PdfDocument();
            //  int height = 842;
            //int width = 595;
            int height = 1010;
            int width = 714;
            int reqH, reqW;
            reqW = width;

            for (int i = 0; i < mArrayUri.size(); i++) {
                Log.e("path", mArrayUri.get(i).getPath());
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mArrayUri.get(i));
                    height = bitmap.getHeight();
                    width = bitmap.getWidth();
                    reqW = width;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reqH = width * bitmap.getHeight() / bitmap.getWidth();
                Log.e("reqH", "=" + reqH);
                if (reqH < height) {
                    //  bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
                } else {
                    reqH = height;
                    reqW = height * bitmap.getWidth() / bitmap.getHeight();
                    Log.e("reqW", "=" + reqW);
                    //   bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
                }
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(reqW, reqH, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                canvas.drawBitmap(bitmap, 0, 0, null);

                document.finishPage(page);
            }

            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file + fileName);
                document.writeTo(fos);
                document.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                dialog.dismiss();
                Toast.makeText(this, "Successful Pdf created", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    public static File commonDocumentDirPath() {
        File dir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
        } else {
            dir = new File(Environment.getExternalStorageDirectory().toString());
        }

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            boolean success = dir.mkdirs();
            if (!success) {
                dir = null;
            }
        }
        return dir;
    }
}