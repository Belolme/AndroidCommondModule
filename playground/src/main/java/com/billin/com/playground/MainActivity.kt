package com.billin.com.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.billin.com.playground.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.helloText.text = "Hello World!"
    }
}
