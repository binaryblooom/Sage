package io.github.junrdev.sage

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import io.github.junrdev.sage.activities.HomeScreen
import io.github.junrdev.sage.activities.Register
import io.github.junrdev.sage.databinding.ActivityMainBinding
import io.github.junrdev.sage.util.Constants

class MainActivity : AppCompatActivity() {

    private val interval: Long = 3000

    private lateinit var checkProfile: RelativeLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkProfile = findViewById(R.id.checkProfile)

        Handler().postDelayed({
            if (Constants.auth.currentUser == null)
                startActivity(Intent(this, Register::class.java))
            else
                startActivity(Intent(this, HomeScreen::class.java))
        }, interval);

    }
}