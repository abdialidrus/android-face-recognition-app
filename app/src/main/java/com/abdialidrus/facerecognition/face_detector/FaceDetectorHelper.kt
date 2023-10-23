package com.abdialidrus.facerecognition.face_detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import com.abdialidrus.facerecognition.face_recognition.FaceClassifier
import com.abdialidrus.facerecognition.face_recognition.TFLiteFaceRecognition
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectorHelper {

    // High-accuracy landmark detection and face classification
    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private lateinit var faceDetector: FaceDetector

    // TODO declare face recognizer
    private lateinit var faceClassifier: FaceClassifier

    fun initialize(context: Context): FaceDetectorHelper {
        // TODO Initialize the face detector
        faceDetector = FaceDetection.getClient(highAccuracyOpts)

        // TODO Initialize face recognition model
        faceClassifier = TFLiteFaceRecognition.create(
            context.assets,
            "facenet.tflite",
            160,
            false
        )

        return this
    }

    fun processBitmapAndRecognize(
        bitmap: Bitmap,
        rotationDegrees: Int = 0,
        onSuccessListener: (newImageWithFaceBoundaries: Bitmap) -> Unit,
        onFailureListener: (Exception) -> Unit
    ) {
        val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                Log.d(
                    "RegistrationActivity",
                    "performFaceDetection: OnSuccess: faces -> $faces"
                )
                val bounds = faces.map { it.boundingBox }
                val newImageWithFaceBoundaries = getAndRecognizeImageWithFaceBoundaries(bounds, bitmap)
                onSuccessListener(newImageWithFaceBoundaries)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.e("RegistrationActivity", "performFaceDetection: OnFailure", e)
                onFailureListener(e)
            }
    }

    fun processBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int = 0,
        onSuccessListener: (croppedFaceList: List<Bitmap>, newImageWithFaceBoundaries: Bitmap) -> Unit,
        onFailureListener: (Exception) -> Unit
    ) {
        val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                Log.d(
                    "RegistrationActivity",
                    "performFaceDetection: OnSuccess: faces -> $faces"
                )
                val bounds = faces.map { it.boundingBox }
                val croppedFaces = getCroppedFaces(bounds, bitmap)
                val newImageWithFaceBoundaries = getImageWithFaceBoundaries(bounds, bitmap)
                onSuccessListener(croppedFaces, newImageWithFaceBoundaries)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.e("RegistrationActivity", "performFaceDetection: OnFailure", e)
                onFailureListener(e)
            }
    }

    private fun getCroppedFaces(bounds: List<Rect>, originalImageBitmap: Bitmap): List<Bitmap> {
        val croppedFaceList = mutableListOf<Bitmap>()
        bounds.forEach { bound ->
            if (bound.top < 0) bound.top = 0
            if (bound.left < 0) bound.left = 0
            if (bound.right > originalImageBitmap.width) bound.right = originalImageBitmap.width - 1
            if (bound.bottom > originalImageBitmap.height) bound.bottom =
                originalImageBitmap.height - 1

            val croppedFace = Bitmap.createBitmap(
                originalImageBitmap,
                bound.left,
                bound.top,
                bound.width(),
                bound.height()
            )
            croppedFaceList.add(croppedFace)
        }

        return croppedFaceList
    }

    private fun getImageWithFaceBoundaries(bounds: List<Rect>, originalImageBitmap: Bitmap): Bitmap {
        val mutableBitmap = originalImageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        bounds.forEach { bound ->
            val paint = Paint()
            paint.color = Color.GREEN
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2F

            canvas.drawRect(bound, paint)
        }

        return mutableBitmap
    }

    private fun getAndRecognizeImageWithFaceBoundaries(bounds: List<Rect>, originalImageBitmap: Bitmap): Bitmap {
        val mutableBitmap = originalImageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        bounds.forEach { bound ->
            if (bound.top < 0) bound.top = 0
            if (bound.left < 0) bound.left = 0
            if (bound.right > originalImageBitmap.width) bound.right = originalImageBitmap.width - 1
            if (bound.bottom > originalImageBitmap.height) bound.bottom =
                originalImageBitmap.height - 1

            val croppedFace = Bitmap.createBitmap(
                originalImageBitmap,
                bound.left,
                bound.top,
                bound.width(),
                bound.height()
            )
            val recognition = recognizeImage(croppedFace)
            val title = recognition?.title
            val distance = recognition?.distance

            val paint = Paint()
            if (distance != null && distance < 1) {
                paint.color = Color.GREEN
                title?.let {
                    val paintText = Paint()
                    paintText.color = Color.WHITE
                    paintText.textSize = 15F
                    canvas.drawText(it, bound.left.toFloat(), bound.bottom.toFloat() + 20F, paintText)
                }
            } else {
                paint.color = Color.RED
            }
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2F

            canvas.drawRect(bound, paint)
        }

        return mutableBitmap
    }

    private fun recognizeImage(originImageBitmap: Bitmap, storeExtra: Boolean = false): FaceClassifier.Recognition? {
        val scaledBitmap = Bitmap.createScaledBitmap(originImageBitmap, 160, 160, false)
        return faceClassifier.recognizeImage(scaledBitmap, storeExtra)
    }
}