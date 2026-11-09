package com.example.spotmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.spotmate.ui.mypage.MyPageFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyPageFragment())
                .commit()
        }
    }
}