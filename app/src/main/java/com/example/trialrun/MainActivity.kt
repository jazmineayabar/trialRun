package com.example.trialrun

//import android.R.*
import android.content.Intent
import android.os.Bundle
//import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
//import androidx.activity.ComponentActivity



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Read Button
        val readButton = findViewById<Button>(R.id.readBtn)
        readButton.setOnClickListener{
            val Intent1 = Intent(this, ReadActivity::class.java)
            startActivity(Intent1)
        }
        //Write Button
        val writeButton = findViewById<Button>(R.id.writeBtn)
        writeButton.setOnClickListener{
            val Intent2 = Intent(this, WriteActivity::class.java)
            startActivity(Intent2)
        }
    }
}

