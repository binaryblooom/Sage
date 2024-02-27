package io.github.junrdev.sage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.android.material.progressindicator.CircularProgressIndicator
import io.github.junrdev.sage.activities.HomeScreen
import io.github.junrdev.sage.activities.Login
import io.github.junrdev.sage.util.Constants.auth

class SplashScreen : AppCompatActivity() {

    private val interval = 3000L
    private val loadingInterval = 2000L
    private lateinit var splashloading: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        splashloading = findViewById(R.id.splashloading)

        Handler().postDelayed({
            splashloading.visibility = View.GONE
            if (auth.currentUser != null) {
                startActivity(Intent(this, HomeScreen::class.java))
                finish()
            } else {
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }, interval)

        Handler().postDelayed({
            splashloading.visibility = View.VISIBLE
        }, loadingInterval)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (auth.currentUser!=null)
            finish()
    }
}