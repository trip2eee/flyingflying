package com.example.flying

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.flying.models.ObjectFileReader
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent_game = Intent(this, GameActivity::class.java)
        val btnStart : Button = findViewById(R.id.btnStart)
        btnStart.setOnClickListener{
            startActivity(intent_game)



        }

    }
}