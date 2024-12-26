package com.taptap.sponsorle

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taptap.sponsorle.databinding.ActivityPlayBinding

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding
    private var score = 0
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }

        startCountdown()
        setupTapDetection()
    }

    private fun startCountdown() {
        val countdownValues = listOf("3", "2", "1", "Go!")
        var index = 0

        val countdownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = countdownValues[index]
                index++
                animateCountdownText() // Apply animation on each tick
            }

            override fun onFinish() {
                binding.tvCountdown.visibility = View.GONE
                binding.tvScore.visibility = View.VISIBLE
                binding.tvTimer.visibility = View.VISIBLE
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
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("FINAL_SCORE", score)
        startActivity(intent)
        finish()
    }
}
