package com.abdialidrus.facerecognition

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.abdialidrus.facerecognition.face_recognition.FaceClassifier.Recognition


class MainActivity : AppCompatActivity() {

    companion object {
        val registered = HashMap<String, Recognition>()
    }

    private lateinit var btnRegister: Button
    private lateinit var btnRecognize: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRegister = findViewById(R.id.btn_register)
        btnRecognize = findViewById(R.id.btn_recognize)

        btnRegister.setOnClickListener {
            navigateToActivity(RegistrationActivity::class.java)
        }

        btnRecognize.setOnClickListener {
            navigateToActivity(RecognitionActivity::class.java)
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume: registered recognition total -> ${registered.size}")
    }

    private fun <T> navigateToActivity(destination: Class<T>) {
        val intent = Intent(this, destination)
        startActivity(intent)
    }
}