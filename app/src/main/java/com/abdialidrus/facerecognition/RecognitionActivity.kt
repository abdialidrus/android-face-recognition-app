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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abdialidrus.facerecognition.face_detector.FaceDetectorHelper
import com.abdialidrus.facerecognition.face_recognition.FaceClassifier
import com.abdialidrus.facerecognition.face_recognition.TFLiteFaceRecognition

class RecognitionActivity : AppCompatActivity(), CroppedFacesAdapter.Interaction {

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
        setContentView(R.layout.activity_recognition)

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
                LinearLayoutManager(this@RecognitionActivity, RecyclerView.HORIZONTAL, false)
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
            faceDetectorHelper.processBitmapAndRecognize(
                bitmap = imageBitmap,
                onSuccessListener = { newImageWithFaceBoundaries ->
                    ivOriginalImage.setImageBitmap(newImageWithFaceBoundaries)
                },
                onFailureListener = {

                }
            )
//            faceDetectorHelper.processBitmap(
//                bitmap = imageBitmap,
//                onSuccessListener = { croppedFaceList, newImageWithFaceBoundaries ->
//                    if (croppedFaceList.isEmpty()) {
//                        noFacesDetected()
//                    } else {
//                        tvFaceDetectionResult.text =
//                            resources.getString(R.string.faces_detected, croppedFaceList.size)
//                    }
//                    populateCroppedFacesList(croppedFaceList)
//                    ivOriginalImage.setImageBitmap(newImageWithFaceBoundaries)
//                },
//                onFailureListener = { _ ->
//
//                }
//            )
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

    private fun showFaceDetailDialog(face: Bitmap, title: String?, distance: Float?) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_face_detail)
        val image: ImageView = dialog.findViewById(R.id.iv_cropped_dialog)
        val tvFaceTitle: TextView = dialog.findViewById(R.id.tv_face_title)

        image.setImageBitmap(face)
        tvFaceTitle.text = "$title ($distance)"

        dialog.show()
    }

    override fun onImageClicked(bitmap: Bitmap) {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 160, 160, false)
        val recognition = faceClassifier.recognizeImage(scaledBitmap, false)
        val title = recognition?.title
        val distance = recognition?.distance
        if (recognition != null) {
            showFaceDetailDialog(bitmap, title, distance)
        }
    }
}