package com.example.chitchat

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.runBlocking

class aichat : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aichat)

        val eTPrompt= findViewById<EditText>(R.id.eTPrompt)
        val btnSubmit= findViewById<ImageButton>(R.id.btnSubmit)
        val aibtnback= findViewById<ImageButton>(R.id.aibackbtn)
        val tVResult= findViewById<TextView>(R.id.tVResult)

        aibtnback.setOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }

        btnSubmit.setOnClickListener {
            val prompt= eTPrompt.text.toString()

            val generativeModel = GenerativeModel(
                // For text-only input, use the gemini-pro model
                modelName = "gemini-pro",
                apiKey = "AIzaSyAIBGJfoIl7gTV523ID737SDubU1QgDw44"

            )
            runBlocking {
                val response = generativeModel.generateContent(prompt)
                tVResult.text= response.text
            }
        }
    }
}