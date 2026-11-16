package com.example.spotmate

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spotmate.login.CloudFirestoreActivity
import com.example.spotmate.login.CloudStorageActivity
import com.example.spotmate.login.GoogleSignInActivity
import com.example.spotmate.login.PWAuthActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto login: Firebase keeps the session, so a non-null user means we are still signed in.
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PWAuthActivity::class.java))
            finish()
            return
        }

        WindowCompat.enableEdgeToEdge(window)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.password_authentication_button).setOnClickListener {
            startActivity(Intent(this@MainActivity, PWAuthActivity::class.java))
        }

        findViewById<View>(R.id.google_button).setOnClickListener {
            startActivity(Intent(this@MainActivity, GoogleSignInActivity::class.java))
        }

        findViewById<View>(R.id.storage_button).setOnClickListener {
            startActivity(Intent(this@MainActivity, CloudStorageActivity::class.java))
        }

        findViewById<View>(R.id.cloud_firestore_button).setOnClickListener {
            startActivity(Intent(this@MainActivity, CloudFirestoreActivity::class.java))
        }

        findViewById<View>(R.id.sign_out_button).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(this@MainActivity, PWAuthActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}
