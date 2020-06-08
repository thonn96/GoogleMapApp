package com.example.kotlinmap

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kotlinmap.Remote.IGoogleAPIService
import kotlinx.android.synthetic.main.activity_view_place.*

class ViewPlace : AppCompatActivity() {

    internal lateinit var mService:IGoogleAPIService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_place)

        btn_view_direction.setOnClickListener {
            val viewDirections = Intent(this,ViewDirections::class.java)
            startActivity(viewDirections)
        }

    }
}
