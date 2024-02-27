package io.github.junrdev.sage.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.junrdev.sage.R
import io.github.junrdev.sage.databinding.ActivityRegisterBinding
import io.github.junrdev.sage.model.User
import io.github.junrdev.sage.util.Constants
import io.github.junrdev.sage.util.Constants.auth
import io.github.junrdev.sage.util.Constants.usersmetadata

private const val TAG = "Register"

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        if (Constants.auth.currentUser == null)
            startActivity(Intent(this, Register::class.java))
        else
            startActivity(Intent(this, HomeScreen::class.java))

        binding.apply {
            signUpBtn.setOnClickListener {
                if (email.text.toString().isEmpty() || password.text.toString().isEmpty())
                    Toast.makeText(
                        applicationContext,
                        "All fields are required.",
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    onClick(email.text.toString(), password.text.toString())
                }
            }
        }
    }

    fun onClick(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnFailureListener {
                Log.d(TAG, "onClick: ${it.localizedMessage}")
            }.addOnCompleteListener { authResultTask ->
                if (authResultTask.isSuccessful && authResultTask.isComplete) {
                    usersmetadata.document(auth.currentUser?.uid!!)
                        .set(User(uid = auth.uid!!))
                        .addOnCompleteListener { metaTask ->
                            if (metaTask.isSuccessful && metaTask.isComplete) {
                                startActivity(Intent(this, HomeScreen::class.java))
                                finish()
                            }
                        }.addOnFailureListener {
                            Log.d(TAG, "onClick: ${it.localizedMessage}")
                        }
                }
            }
    }
}