package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taptap.sponsorle.databinding.ActivityScratchBinding

class ScratchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScratchBinding
    private var scratchPercentage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScratchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        setupScratchCard()
        setupAddToWalletButton()
    }

    private fun setupScratchCard() {
        // Simulate scratch card behavior
        binding.llScratchCard.setOnTouchListener { _, _ ->
            scratchPercentage += 10 // Simulate scratching progress
            if (scratchPercentage >= 70) {
                showFlameCredits()
            }
            true
        }
    }

    private fun showFlameCredits() {
        // Add fade-out animation for llSpin
        val fadeOut = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 500 // 500ms
            fillAfter = true
        }
        binding.llSpin.startAnimation(fadeOut)
        binding.llSpin.visibility = View.GONE

        // Add fade-in animation for llFlameCredit
        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 500 // 500ms
            fillAfter = true
        }
        binding.llFlameCredit.visibility = View.VISIBLE
        binding.llFlameCredit.startAnimation(fadeIn)
    }

    private fun setupAddToWalletButton() {
        binding.cvAddToWallet.setOnClickListener {
            // Navigate to Add Wallet Activity
            val intent = Intent(this, Add_WalletActivity::class.java)
            startActivity(intent)
        }
    }
}
