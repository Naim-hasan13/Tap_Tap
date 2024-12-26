package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taptap.sponsorle.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.cvSpin.setOnClickListener {
            val intent = Intent(this, SpinActivity::class.java)
            startActivity(intent)

        }
        binding.cvPromocode.setOnClickListener {
            val intent = Intent(this, PromocodeActivity::class.java)
            startActivity(intent)

        }
        binding.cvCaptcha.setOnClickListener {
            val intent = Intent(this, captchaActivity::class.java)
            startActivity(intent)

        }
        binding.cvRefer.setOnClickListener {
            val intent = Intent(this, ReferActivity::class.java)
            startActivity(intent)
        }
            binding.cvSocialMedia.setOnClickListener {
                val intent = Intent(this, Social_mediaActivity::class.java)
                startActivity(intent)

            }

            binding.cvCheckIn.setOnClickListener {
                val intent = Intent(this, CheckInActivity::class.java)
                startActivity(intent)

            }

            binding.cvScratch.setOnClickListener {
                val intent = Intent(this, ScratchActivity::class.java)
                startActivity(intent)

            }

    }
}