package com.chandra.practicefunctionalities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chandra.practicefunctionalities.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private  var TAG = "MainActivity"
    private lateinit var binding : ActivityMainBinding
    private var progressDialog : ProgressDialog? = null

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cameraCaptureButton.setOnClickListener {
            val intent = Intent(this , CameraActivity::class.java)
            startActivity(intent)
        }
        try {
            if (progressDialog != null && progressDialog !!.isShowing) {
                progressDialog !!.dismiss()
            }
            val uriToBitMap : Uri?= intent.getParcelableExtra("uri")
            Log.d(TAG , "onCreate: $uriToBitMap")

            val bitmap =
                MediaStore.Images.Media.getBitmap(contentResolver , uriToBitMap!!)

            binding.imageViewData.setImageBitmap(bitmap)
        }catch (e : Exception){
            e.stackTrace
        }
    }
}