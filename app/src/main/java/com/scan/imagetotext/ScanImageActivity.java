package com.scan.imagetotext;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class ScanImageActivity extends AppCompatActivity {
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 292;


    PreviewView mPreviewView;

    ImageView captureImage;

    LinearLayout cameraLayout;

    String imageurl;
    Uri uri;

    ImageView imageView;

    Bitmap myBitmap;

    //for camera
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    View bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        setTitle("Image To Text");
        ImageView imageView = findViewById(R.id.imageID);
        bar = findViewById(R.id.bar);
        cameraLayout = findViewById(R.id.camera_LayoutID);
        captureImage = findViewById(R.id.captureImg);
        PreviewView mPreviewView = findViewById(R.id.camera);
        chooseImage(this);

    }

    // function to let's the user to choose image from camera or gallery
    private void chooseImage(Context context) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_choice);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        Button btnCancel = dialog.findViewById(R.id.btnCancle);
        LinearLayout cameraIv, galleryIv;

        cameraIv = dialog.findViewById(R.id.ivCamera);
        galleryIv = dialog.findViewById(R.id.ivGallary);

        cameraIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (methodRequiresTwoPermission() && allPermissionsGranted()) {
                    cameraLayout.setVisibility(View.VISIBLE);
                    startCamera();
                    dialog.dismiss();

                    //start camera if permission has been granted by user
                } else {
                    ActivityCompat.requestPermissions(ScanImageActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

                }
            }
        });

        galleryIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allPermissionsGranted() && methodRequiresTwoPermission()) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    imagePickerLuncher.launch(pickPhoto);
                    dialog.dismiss();

                } else {
                    ActivityCompat.requestPermissions(ScanImageActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private boolean methodRequiresTwoPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                REQUIRED_PERMISSIONS[0]
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            launcherPermission.launch(REQUIRED_PERMISSIONS);
            return false;
        }
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    ActivityResultLauncher<String[]> launcherPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), granted -> {

                if (ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[2]) == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
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
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                myBitmap = BitmapFactory.decodeFile(picturePath);
                                imageView.setImageBitmap(myBitmap);
                                imageView.setVisibility(View.VISIBLE);
                                starAnimation();
                                findTextFromImage(myBitmap);
                                imageurl = picturePath;
                                cursor.close();
                            }
                        }
                    }
                }
            });


    public void starAnimation() {
        final Animation animation = AnimationUtils.loadAnimation(ScanImageActivity.this, R.anim.scan_animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // bar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        bar.startAnimation(animation);
    }

    //take image from camera
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);


        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreviewView.setVisibility(View.VISIBLE);

                SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date()) + ".jpg");
                uri = Uri.fromFile(file);
                Log.e("uri", String.valueOf(uri));
                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Handler h = new Handler(Looper.getMainLooper());
                        h.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Image Saved successfully", Toast.LENGTH_SHORT).show();
                                //here show dialog
                                myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                cameraLayout.setVisibility(View.GONE);
                                imageView.setImageBitmap(myBitmap);
                                imageView.setVisibility(View.VISIBLE);
                                starAnimation();
                                imageurl = file.getAbsolutePath();
                                findTextFromImage(myBitmap);
                                Log.e("imageurl", imageurl);
                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()},
                                        null,
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            @Override
                                            public void onScanCompleted(String path, Uri uri) {

                                            }
                                        });
                            }
                        });

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        error.printStackTrace();
                    }
                });
            }
        });
    }

    private void findTextFromImage(Bitmap bitmap) {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

      /*  FirebaseVisionCloudDocumentRecognizerOptions options =
                new FirebaseVisionCloudDocumentRecognizerOptions.Builder()
                        .setLanguageHints(Arrays.asList("en", "bn", "hi"))
                        .build();
        FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer(options);

        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        result.getText().toString();
                        Log.e("findText", result.getText().toString());
                        Toast.makeText(MainActivity.this, result.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        Log.e("findText", e.toString());

                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        // ...
                    }
                });*/
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                Log.e("findText", firebaseVisionText.getText().toString());
                                startActivity(new Intent(getApplicationContext(), ResultActivity.class).putExtra("result", firebaseVisionText.getText()));
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
    }

    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }
        return app_folder_path;

    }

    public void ScanNow(View view) {
    }
}