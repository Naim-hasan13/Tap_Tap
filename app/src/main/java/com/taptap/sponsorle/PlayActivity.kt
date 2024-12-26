package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.taptap.sponsorle.databinding.ActivityPlayBinding
import com.taptap.sponsorle.extrazz.TinyDB

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding
    private var score = 0
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCountdown()
    }

    private fun startCountdown() {
        val countdownValues = listOf("3", "2", "1", "Go!")
        var index = 0

        val countdownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = countdownValues[index]
                index++
                animateCountdownText() // Apply animation on each tick
            }

            override fun onFinish() {
                binding.tvCountdown.visibility = View.GONE
                binding.tvScore.visibility = View.VISIBLE
                binding.tvTimer.visibility = View.VISIBLE
                setupTapDetection()
                startGameTimer()
            }
        }
        countdownTimer.start()
    }

    private fun animateCountdownText() {
        binding.tvCountdown.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .alpha(1f)
            .setDuration(500)
            .withEndAction {
                binding.tvCountdown.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            }
            .start()
    }


    private fun startGameTimer() {
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = "Time: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                endGame()
            }
        }
        timer?.start()
    }

    private fun setupTapDetection() {
        binding.playLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                score++
                binding.tvScore.text = "Score: $score"
                performScreenShrinkAnimation()
            }
            true
        }
    }

    private fun performScreenShrinkAnimation() {
        binding.playLayout.animate()
            .scaleX(0.95f) // Shrink horizontally
            .scaleY(0.95f) // Shrink vertically
            .setDuration(100) // Animation duration for shrink
            .withEndAction {
                binding.playLayout.animate()
                    .scaleX(1f) // Return to original size
                    .scaleY(1f)
                    .setDuration(100) // Animation duration for return
                    .start()
            }
            .start()
    }


    private fun endGame() {
        timer?.cancel()
        val heightScore = TinyDB.getInt(this, "high", 0)
        if (heightScore < score) {
            TinyDB.saveInt(this, "high", score)
        }
        TinyDB.saveInt(this, "total_score", TinyDB.getInt(this, "total_score", 0) + score)
        TinyDB.saveInt(this, "score", score)
        startActivity(Intent(this, ResultActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
