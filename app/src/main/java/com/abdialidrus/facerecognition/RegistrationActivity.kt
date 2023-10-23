package com.abdialidrus.facerecognition

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abdialidrus.facerecognition.face_detector.FaceDetectorHelper
import com.abdialidrus.facerecognition.face_recognition.FaceClassifier
import com.abdialidrus.facerecognition.face_recognition.TFLiteFaceRecognition
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegistrationActivity : AppCompatActivity(), CroppedFacesAdapter.Interaction {

    private lateinit var ivOriginalImage: ImageView
    private lateinit var rvCroppedFaces: RecyclerView
    private lateinit var tvFaceDetectionResult: TextView
    private lateinit var btnOpenGallery: Button
    private lateinit var btnOpenCamera: Button
    private var uriOriginalImage: Uri? = null
    private val listCroppedFace: MutableList<Bitmap> = mutableListOf()
    private val adapterCroppedFaceList = CroppedFacesAdapter(listCroppedFace, this)

    //TODO get the image from gallery and display it
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                uriOriginalImage = activityResult.data?.data
                val imageBitmap = convertImageUriToBitmap(
                    context = this,
                    imageUri = uriOriginalImage,
                )
                ivOriginalImage.setImageBitmap(imageBitmap)
                performFaceDetection(imageBitmap)
            }
        }

    //TODO capture the image using camera and display it
    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                val imageBitmap = convertImageUriToBitmap(
                    context = this,
                    imageUri = uriOriginalImage,
                )
                ivOriginalImage.setImageBitmap(imageBitmap)
                performFaceDetection(imageBitmap)
            }
        }

    // TODO declare the face detector
    private lateinit var faceDetectorHelper: FaceDetectorHelper

    // TODO declare face recognizer
    private lateinit var faceClassifier: FaceClassifier


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        ivOriginalImage = findViewById(R.id.imageView)
        tvFaceDetectionResult = findViewById(R.id.tv_result)
        btnOpenGallery = findViewById(R.id.btn_gallery)
        btnOpenCamera = findViewById(R.id.btn_camera)

        btnOpenGallery.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryActivityResultLauncher.launch(galleryIntent)
        }

        btnOpenCamera.setOnClickListener {
            if (isCameraPermissionGranted()) {
                uriOriginalImage = openCamera(this, cameraActivityResultLauncher)
            }
        }

        rvCroppedFaces = findViewById(R.id.rv_cropped_faces)
        rvCroppedFaces.apply {
            layoutManager =
                LinearLayoutManager(this@RegistrationActivity, RecyclerView.HORIZONTAL, false)
            adapter = adapterCroppedFaceList
        }

        // TODO Initialize the face detector
        faceDetectorHelper = FaceDetectorHelper().initialize(this)

        // TODO Initialize face recognition model
        faceClassifier = TFLiteFaceRecognition.create(
            assets,
            "facenet.tflite",
            160,
            false
        )
    }

    // TODO perform face detection
    private fun performFaceDetection(imageBitmap: Bitmap?) {
        if (imageBitmap != null) {
            faceDetectorHelper.processBitmap(
                bitmap = imageBitmap,
                onSuccessListener = { croppedFaceList, newImageWithFaceBoundaries ->
                    if (croppedFaceList.isEmpty()) {
                        noFacesDetected()
                    } else {
                        tvFaceDetectionResult.text = resources.getString(R.string.faces_detected, croppedFaceList.size)
                    }
                    populateCroppedFacesList(croppedFaceList)
                    ivOriginalImage.setImageBitmap(newImageWithFaceBoundaries)
                },
                onFailureListener = { _ ->

                }
            )
        } else {
            Log.e("RegistrationActivity", "performFaceDetection is failed, input bitmap is null")
        }
    }

    private fun populateCroppedFacesList(croppedFaceList: List<Bitmap>) {
        listCroppedFace.clear()
        listCroppedFace.addAll(croppedFaceList)
        adapterCroppedFaceList.notifyDataSetChanged()
    }

    private fun noFacesDetected() {
        tvFaceDetectionResult.text = resources.getString(R.string.no_faces_detected)
    }

    private fun showRegisterDialog(face: Bitmap, recognition: FaceClassifier.Recognition) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_register_face)
        val image: ImageView = dialog.findViewById(R.id.iv_cropped_dialog)
        val til: TextInputLayout = dialog.findViewById(R.id.til_name)
        val editText: TextInputEditText = dialog.findViewById(R.id.et_face_name)
        val btnRegister: Button = dialog.findViewById(R.id.btn_register)

        image.setImageBitmap(face)
        btnRegister.setOnClickListener {
            if (editText.text.isNullOrBlank() || editText.text.isNullOrEmpty()) {
                til.error = "Please enter the name"
            } else {
                faceClassifier.register(editText.text.toString(), recognition)
                Toast.makeText(this@RegistrationActivity, "Face is registered", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onImageClicked(bitmap: Bitmap) {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 160, 160, false)
        val recognition = faceClassifier.recognizeImage(scaledBitmap, true)
        if (recognition != null) {
            showRegisterDialog(bitmap, recognition)
        }
    }
}