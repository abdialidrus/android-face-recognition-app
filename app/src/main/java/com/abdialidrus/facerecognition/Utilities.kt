package com.abdialidrus.facerecognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import java.io.IOException

fun Activity.isCameraPermissionGranted(): Boolean {
    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_DENIED
    ) {
        val permission =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permission, 112)

        return false
    }

    return true
}

//TODO takes URI of the image and returns bitmap
fun uriToBitmap(context: Context, selectedFileUri: Uri?): Bitmap? {
    if (selectedFileUri != null) {
        try {
            val parcelFileDescriptor =
                context.contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return null
}

//TODO rotate image if image captured on samsung devices
//TODO Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
@SuppressLint("Range")
fun rotateBitmap(context: Context, input: Bitmap?, imageUri: Uri?): Bitmap? {
    if (input != null) {
        val orientationColumn =
            arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cur =
            imageUri?.let { context.contentResolver.query(it, orientationColumn, null, null, null) }
        var orientation = -1
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]))
        }
        Log.d("tryOrientation", orientation.toString() + "")
        val rotationMatrix = Matrix()
        rotationMatrix.setRotate(orientation.toFloat())
        return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
    } else {
        return null
    }
}

fun openCamera(
    context: Context,
    cameraActivityResultLauncher: ActivityResultLauncher<Intent>
): Uri? {
    val imageUri: Uri?
    val values = ContentValues()
    values.put(MediaStore.Images.Media.TITLE, "New Picture")
    values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
    imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
    cameraActivityResultLauncher.launch(cameraIntent)

    return imageUri
}

fun convertImageUriToBitmap(context: Context, imageUri: Uri?): Bitmap? {
    var inputImage = uriToBitmap(context, imageUri)
    inputImage = rotateBitmap(context, inputImage, imageUri)
    return inputImage
}