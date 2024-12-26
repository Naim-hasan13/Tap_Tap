package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taptap.sponsorle.databinding.ActivityHomeBinding
import com.taptap.sponsorle.extrazz.TinyDB

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        binding.tvCount.text = TinyDB.getInt(this, "total_score", 0).toString()
        binding.tvHighestCount.text = TinyDB.getInt(this, "high", 0).toString()

        binding.llStart.setOnClickListener {
            val intent = Intent(this, PlayActivity::class.java)
            startActivity(intent)
        }
        binding.cvMenu.setOnClickListener {
            if (TinyDB.getInt(this, "total_score", 0) > 1500) {
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvCount.text = TinyDB.getInt(this, "total_score", 0).toString()
        binding.tvHighestCount.text = TinyDB.getInt(this, "high", 0).toString()

    }

}