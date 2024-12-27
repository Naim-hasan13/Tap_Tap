package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taptap.sponsorle.databinding.ActivityReferBinding
import com.taptap.sponsorle.extrazz.Companions
import com.taptap.sponsorle.extrazz.TinyDB

class ReferActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReferBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        binding.ivShare.setOnClickListener {
            shareApp()
        }
       binding.ivFacebook.setOnClickListener {
            shareApp()
        }
       binding.ivWhatsapp.setOnClickListener {
            shareApp()
        }
       binding.ivYoutube.setOnClickListener {
            shareApp()
        }
        binding.tvTotalUser.text = TinyDB.getString(this, "total_registration", "0")
    }

    fun shareApp() {
        val referCode = TinyDB.getString(this, "refer_code", "")

        // Build the referral link with the refer code
        val shareMessage = "Download this amazing app with this link to get a bonus: " +
                "https://selfapp.earningads.io/v3/app/?code=$referCode&app=${Companions.APP_ID}"

        // Create an Intent to share the message
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        // Check if there is an app that can handle the intent
        startActivity(Intent.createChooser(shareIntent, "Share via"))

    }
}
