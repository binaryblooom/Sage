package io.github.junrdev.sage.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import io.github.junrdev.sage.R
import io.github.junrdev.sage.util.Constants.auth

private const val TAG = "Login"

class Login : AppCompatActivity() {

    private lateinit var password: TextInputEditText
    private lateinit var email: TextInputEditText
    private lateinit var signInProgress: CircularProgressIndicator
    private lateinit var createAccount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        password = findViewById(R.id.password)
        createAccount = findViewById(R.id.createAccount)
        email = findViewById(R.id.email)
        signInProgress = findViewById(R.id.signInProgress)

        if (auth.currentUser != null){
            startActivity(Intent(this, HomeScreen::class.java))
            finish()
        }
        createAccount.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }

    fun signInUser(view: View) {
        Log.d(TAG, "onCreate: ${email.text}")
        if (email.text.toString().isEmpty() || password.text.toString().isEmpty())
            Toast.makeText(applicationContext, "All fields are required.", Toast.LENGTH_SHORT)
                .show()
        else {
            signInProgress.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful && it.isComplete) {
                        signInProgress.visibility = View.GONE
                        startActivity(Intent(applicationContext, HomeScreen::class.java))
                        finish()
                    }
                }.addOnFailureListener {
                    signInProgress.visibility = View.GONE
                    Log.d(TAG, "onCreate: ${it.localizedMessage}")
                }
        }
    }
}

