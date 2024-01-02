package com.chandra.practicefunctionalities

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.chandra.practicefunctionalities.databinding.ActivityCameraBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma : Double) -> Unit

class CameraActivity : AppCompatActivity() {
    private val TAG = "CameraActivity"
    private lateinit var binding : ActivityCameraBinding
    private var preView : PreviewView? = null
    private var imageCapture = ImageCapture.Builder().build()
    private lateinit var cameraExecutor : ExecutorService
    var progressDialog : ProgressDialog? = null
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Request camera permissions
        val CAMERA_PERMISSION_CODE = 101 // You can define any value for this code

        if (ContextCompat.checkSelfPermission(
                    this ,
                    Manifest.permission.CAMERA
                                             ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                    this ,
                    arrayOf(Manifest.permission.CAMERA) ,
                    CAMERA_PERMISSION_CODE
                                             )
        } else {
            // Permission has already been granted
            // Initialize and use the camera here
            startCamera()
        }


        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }
        // viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {} , ContextCompat.getMainExecutor(this))
        val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                    this , cameraSelector , preview
                                          )
        } catch (exc : Exception) {
            Log.e(TAG , "Use case binding failed" , exc)
        }
        val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor , LuminosityAnalyzer { luma ->
                      //  Log.d(TAG , "Average luminosity: $luma")
                    })
                }
        cameraProvider.bindToLifecycle(
                this , cameraSelector , preview , imageCapture , imageAnalyzer
                                      )

    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.previewView.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder()
                    .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor , LuminosityAnalyzer { luma ->
                           // Log.d(TAG , "Average luminosity: $luma")
                        })
                    }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this , cameraSelector , preview , imageCapture , imageAnalyzer
                                              )

            } catch (exc : Exception) {
                Log.e(TAG , "Use case binding failed" , exc)
            }

        } , ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val dialog = ProgressDialog.show(
                this@CameraActivity , "" ,
                "Image Loading. Please wait..." , true
                                        )
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT , Locale.US)
                .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME , name)
            put(MediaStore.MediaColumns.MIME_TYPE , "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH , "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
                .Builder(
                        contentResolver ,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,
                        contentValues
                        )
                .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions ,
                ContextCompat.getMainExecutor(this) ,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc : ImageCaptureException) {
                        Log.e(TAG , "Photo capture failed: ${exc.message}" , exc)
                        dialog.dismiss()
                    }

                    override fun onImageSaved(output : ImageCapture.OutputFileResults) {
                        val msg = "Photo capture succeeded: ${output.savedUri}"
                        Toast.makeText(baseContext , msg , Toast.LENGTH_SHORT).show()

                        val bitmap =
                            MediaStore.Images.Media.getBitmap(contentResolver , output.savedUri)
                        binding.cameraCaptureButton.visibility = View.GONE
                        binding.previewView.visibility = View.GONE
                        /*supportActionBar?.hide()
                        binding.imageView.visibility = View.VISIBLE*/
                        ///val fileName = "my_image.png"
                        dialog.dismiss()
                        val pd = ProgressDialog(this@CameraActivity)


                        /*val decodedString : ByteArray = Base64.decode(bitmap , Base64.NO_WRAP)
                        val input : InputStream = ByteArrayInputStream(decodedString)
                        val ext_pic = BitmapFactory.decodeStream(input)*/
                       /* binding.imageView.setImageBitmap(bitmap)*/
                       /* Glide.with(this@CameraActivity)
                                .load(output.savedUri) // Replace with the actual URI of the captured image
                                .centerInside()
                                .into(binding.imageView)*/
                        binding.buttonsLayout.visibility = View.VISIBLE
                        binding.imageCancel.visibility = View.VISIBLE

                        binding.imageDone.setOnClickListener {
                            pd.setMessage("Saving image")
                            pd.show()
                            Toast.makeText(baseContext , "Image Saving" , Toast.LENGTH_SHORT).show()

                            storeImage(bitmap)
                           /* val file = File(this@CameraActivity.externalCacheDir , "image.jpg")
                            val fileUri = FileProvider.getUriForFile(this@CameraActivity, "your.fileprovider.authority", file)
                            intent.putExtra("imageUri", fileUri)*/
                           /* val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()*/
                            val intent = Intent(this@CameraActivity, MainActivity::class.java)
                            intent.putExtra("uri" , output.savedUri)
                            pd.dismiss()
                            startActivity(intent)
                        }
                        binding.imageCancel.setOnClickListener {
                            progressDialog = ProgressDialog(this@CameraActivity)
                            progressDialog !!.setMessage("Loading...")
                            progressDialog !!.setCancelable(false)
                            progressDialog !!.show()
                            Toast.makeText(baseContext , "image Canceled" , Toast.LENGTH_SHORT).show()
                            binding.cameraCaptureButton.visibility = View.VISIBLE
                            binding.previewView.visibility = View.VISIBLE
                            binding.imageView.visibility = View.GONE
                            binding.buttonsLayout.visibility=View.GONE
                            /*binding.imageDone.visibility = View.GONE
                            binding.imageCancel.visibility = View.GONE*/
                            startCamera()
                        }

                    }
                }
                                )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun storeImage(image : Bitmap) {
        val pictureFile : File = getOutputMediaFile()
        if (pictureFile == null) {
            Log.d(
                    TAG ,
                    "Error creating media file, check storage permissions: "
                 ) // e.getMessage());
            return
        }
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG , 90 , fos)
            fos.close()
        } catch (e : FileNotFoundException) {
            Log.d(TAG , "File not found: " + e.message)
        } catch (e : IOException) {
            Log.d(TAG , "Error accessing file: " + e.message)
        }
    }

    /** Create a File for saving an image or video  */
    private fun getOutputMediaFile() : File {
        val mediaStorageDir = File(applicationContext.filesDir, "mydir")
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdir()
        }
        
        // Create a media file name
        val timeStamp = SimpleDateFormat("ddMMyyyy_HHmm").format(Date())
        val mediaFile : File
        val mImageName = "MI_$timeStamp.jpg"
        mediaFile = File(mediaStorageDir.path + File.separator + mImageName)
        Log.d(TAG , "getOutputMediaFile: $mediaFile")
        return mediaFile
    }



    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                    Manifest.permission.CAMERA ,
                    Manifest.permission.RECORD_AUDIO
                         ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}