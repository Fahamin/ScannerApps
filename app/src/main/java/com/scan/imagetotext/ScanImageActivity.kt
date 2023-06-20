package com.scan.imagetotext

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCapture.OutputFileResults
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import me.pqpo.smartcropperlib.view.CropImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ScanImageActivity : AppCompatActivity() {
    lateinit var mPreviewView: PreviewView
    lateinit var captureImage: ImageView
    lateinit var cameraLayout: RelativeLayout
    lateinit var imageurl: String
    lateinit var uri: Uri
    lateinit var imageView: CropImageView
    lateinit var btnScan: Button

    lateinit var myBitmap: Bitmap
    lateinit var bar: View
    lateinit var cropBitmap: Bitmap

    //for camera
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUIRED_PERMISSIONS = arrayOf(
        "android.permission.CAMERA",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_image)
        FirebaseApp.initializeApp(this)
        title = "Image To Text"
        imageView = findViewById(R.id.imageID)
        bar = findViewById(R.id.bar)
        cameraLayout = findViewById(R.id.camera_LayoutID)
        captureImage = findViewById(R.id.captureImg)
        mPreviewView = findViewById(R.id.camera)
        btnScan = findViewById(R.id.btn_Scan)
        chooseImage(this)

        btnScan.setOnClickListener(View.OnClickListener {

            starAnimation()
            findTextFromImage(cropBitmap)
        })
    }

    // function to let's the user to choose image from camera or gallery
    private fun chooseImage(context: Context) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_choice)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancle)
        val cameraIv: LinearLayout
        val galleryIv: LinearLayout
        cameraIv = dialog.findViewById(R.id.ivCamera)
        galleryIv = dialog.findViewById(R.id.ivGallary)

        cameraIv.setOnClickListener {
            if (methodRequiresTwoPermission() && allPermissionsGranted()) {
                cameraLayout.visibility = View.VISIBLE
                startCamera()
                dialog.dismiss()

                //start camera if permission has been granted by user
            } else {
                ActivityCompat.requestPermissions(
                    this@ScanImageActivity,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }
        galleryIv.setOnClickListener {
            if (allPermissionsGranted() && methodRequiresTwoPermission()) {
                val pickPhoto =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                imagePickerLuncher.launch(pickPhoto)
                dialog.dismiss()
            } else {
                ActivityCompat.requestPermissions(
                    this@ScanImageActivity,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }
        btnCancel.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun methodRequiresTwoPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                REQUIRED_PERMISSIONS[0]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            launcherPermission.launch(REQUIRED_PERMISSIONS)
            false
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    var launcherPermission = registerForActivityResult<Array<String>, Map<String, Boolean>>(
        ActivityResultContracts.RequestMultiplePermissions(),
        ActivityResultCallback<Map<String, Boolean>> { granted: Map<String, Boolean> ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    REQUIRED_PERMISSIONS[0]
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    REQUIRED_PERMISSIONS[1]
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    REQUIRED_PERMISSIONS[2]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        })

    // Luncher  imagepick from gallary
    var imagePickerLuncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode == RESULT_OK) {
                    // There are no request codes
                    val data = result.data
                    val selectedImage = data!!.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor =
                            contentResolver.query(selectedImage, filePathColumn, null, null, null)
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath = cursor.getString(columnIndex)
                            myBitmap = BitmapFactory.decodeFile(picturePath)
                            imageView.rotation = 0f
                            imageView.setImageToCrop(myBitmap)
                            cropBitmap = imageView.crop()

                            imageView.visibility = View.VISIBLE
                            btnScan.visibility = View.VISIBLE
                            imageurl = picturePath

                            cursor.close()
                        }
                    }
                }
            }
        })

    fun starAnimation() {
        val animation = AnimationUtils.loadAnimation(this@ScanImageActivity, R.anim.scan_animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                // bar.setVisibility(View.GONE);
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        bar.startAnimation(animation)
    }

    //take image from camera
    private fun startCamera() {


        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        //  val preview = Preview.Builder().build()
        val preview = Preview.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
            setTargetRotation(Surface.ROTATION_90)
            setTargetRotation(mPreviewView.display.rotation)
        }.build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .build()

        val builder = ImageCapture.Builder()


        val imageCapture = builder
            .setTargetRotation(this.windowManager.defaultDisplay.rotation)
            .build()
        preview.setSurfaceProvider(mPreviewView.surfaceProvider)

        if (preview != null) {
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis,
                imageCapture
            )
        } else {
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                imageAnalysis,
                imageCapture
            )
        }

        captureImage.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                mPreviewView.visibility = View.VISIBLE
                val mDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
                val file: File = File(batchDirectoryName, mDateFormat.format(Date()) + ".jpg")
                uri = Uri.fromFile(file)
                Log.e("uri", uri.toString())
                val outputFileOptions = OutputFileOptions.Builder(file).build()
                imageCapture.takePicture(
                    outputFileOptions,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: OutputFileResults) {
                            val h = Handler(Looper.getMainLooper())
                            h.post {
                                Toast.makeText(
                                    applicationContext,
                                    "Image Saved successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //here show dialog
                                myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                                cameraLayout.visibility = View.GONE

                                imageView.setImageToCrop(myBitmap)
                                imageView.visibility = View.VISIBLE
                                btnScan.visibility = View.VISIBLE

                                cropBitmap = imageView.crop()
                                imageurl = file.absolutePath
                                //Log.e("imageurl", imageurl)
                                MediaScannerConnection.scanFile(
                                    applicationContext, arrayOf(file.absolutePath),
                                    null
                                ) { path, uri -> }
                            }
                        }

                        override fun onError(error: ImageCaptureException) {
                            error.printStackTrace()
                        }
                    })
            }
        })
    }

    private fun findTextFromImage(bitmap: Bitmap?) {
        val image = FirebaseVisionImage.fromBitmap(bitmap!!)

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
        val detector = FirebaseVision.getInstance()
            .onDeviceTextRecognizer
        val result = detector.processImage(image)
            .addOnSuccessListener(object : OnSuccessListener<FirebaseVisionText> {
                override fun onSuccess(firebaseVisionText: FirebaseVisionText) {
                    // Task completed successfully
                    Log.e("findText", firebaseVisionText.text)
                    startActivity(
                        Intent(
                            applicationContext,
                            ResultActivity::class.java
                        ).putExtra("result", firebaseVisionText.text)
                    )
                }
            })
            .addOnFailureListener {
                // Task failed with an exception
                // ...
            }
    }

    val batchDirectoryName: String
        get() {
            var app_folder_path = ""
            app_folder_path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + "/images"
            val dir = File(app_folder_path)
            if (!dir.exists() && !dir.mkdirs()) {
            }
            return app_folder_path
        }


    companion object {
        private const val REQUEST_ID_MULTIPLE_PERMISSIONS = 292
    }
}