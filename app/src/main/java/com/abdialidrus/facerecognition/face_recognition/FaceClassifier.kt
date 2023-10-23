package com.abdialidrus.facerecognition.face_recognition

import android.graphics.Bitmap
import android.graphics.RectF

interface FaceClassifier {
    fun register(name: String?, recognition: Recognition?)

    fun recognizeImage(bitmap: Bitmap?, getExtra: Boolean): Recognition?

    class Recognition(
        val id: String?,
        /** Display name for the recognition.  */
        var title: String?, distance: Float?, location: RectF?
    ) {

        // A sortable score for how good the recognition is relative to others. Lower should be better.
        val distance: Float?
        var embeeding: Any?

        /** Optional location within the source image for the location of the recognized face.  */
        private var location: RectF?
        var crop: Bitmap?

        init {
            title = title
            this.distance = distance
            this.location = location
            embeeding = null
            crop = null
        }

        fun getLocation(): RectF {
            return RectF(location)
        }

        fun setLocation(location: RectF?) {
            this.location = location
        }

        override fun toString(): String {
            var resultString = ""
            if (id != null) {
                resultString += "[$id] "
            }
            if (title != null) {
                resultString += "$title "
            }
            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f)
            }
            if (location != null) {
                resultString += location.toString() + " "
            }
            return resultString.trim { it <= ' ' }
        }
    }
}