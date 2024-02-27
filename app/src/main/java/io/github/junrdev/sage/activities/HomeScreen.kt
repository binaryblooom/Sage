package io.github.junrdev.sage.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.junrdev.sage.R
import io.github.junrdev.sage.SplashScreen
import io.github.junrdev.sage.activities.fragments.FilterResult
import io.github.junrdev.sage.util.Constants.auth
import io.github.junrdev.sage.util.Constants.favs

class HomeScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        Toast.makeText(applicationContext, "In", Toast.LENGTH_SHORT).show()

    }

    fun openDownloads(view: View) {
        startActivity(Intent(this, Downloads::class.java))
    }

    fun openUploads(view: View) {
        startActivity(Intent(this, Upload::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this, SplashScreen::class.java))
                finish()
                true
            }

            R.id.account -> {
                Toast.makeText(applicationContext, "Notify", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    fun openFavourites(view: View) {
        if (favs.isEmpty()) {
            Toast.makeText(applicationContext, "No items added.", Toast.LENGTH_SHORT).show()
            return
        }

        val favs = Intent(this, FilterResult::class.java)
        favs.putExtra("content", "favs")
        startActivity(favs)
    }

    fun openHowToSection(view: View) {
        Toast.makeText(applicationContext, "Under Implementation", Toast.LENGTH_SHORT).show()
    }
}