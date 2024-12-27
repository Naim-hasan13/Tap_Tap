package com.taptap.sponsorle

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.taptap.sponsorle.databinding.ActivityPlayBinding
import com.taptap.sponsorle.extrazz.TinyDB

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding
    private var score = 0
    private var timer: CountDownTimer? = null
    lateinit var countdownTimer: CountDownTimer
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

        countdownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = countdownValues[index]
                index++
                animateCountdownText() // Apply animation on each tick
            }

            override fun onFinish() {
                binding.tvCountdown.visibility = View.GONE
                binding.tvScore.visibility = View.VISIBLE
                binding.tvTimer.visibility = View.VISIBLE
                binding.llScore.visibility = View.VISIBLE
                setupTapDetection()
                startGameTimer()
            }
        }
        countdownTimer.start()
    }

    fun showBoomImage(q1: FrameLayout, x: Int, y: Int) {
        // Create a new ImageView for the "Bang" image
        val wrongImage = ImageView(this)
        wrongImage.setImageResource(R.drawable.bang) // Set your "bang" image

        // Assign a unique ID to the ImageView
        val imageViewId = View.generateViewId()
        wrongImage.id = imageViewId

        // Set the size of the ImageView
        val params = FrameLayout.LayoutParams(200, 200) // Set size for image
        wrongImage.layoutParams = params

        // Position the image at the touch coordinates
        wrongImage.x = x.toFloat() - 100f // Subtract half of the image width for centering
        wrongImage.y = y.toFloat() - 160f // Subtract half of the image height for centering

        // Add the ImageView to the layout
        q1.addView(wrongImage)

        // Remove the ImageView after 700 milliseconds
        Handler(Looper.getMainLooper()).postDelayed({
            q1.removeView(wrongImage)
        }, 700)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTapDetection() {
        binding.playLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                score++
                binding.tvScore.text = "$score"
                performScreenShrinkAnimation()
                val coordinates = IntArray(2)
                binding.playLayout.getLocationOnScreen(coordinates) // Get screen coordinates of the answer ImageView


                val touchX = event.rawX.toInt()
                val touchY = event.rawY.toInt()

                    showBoomImage(binding.playLayout, touchX, touchY)

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
        countdownTimer.cancel()
    }
}
