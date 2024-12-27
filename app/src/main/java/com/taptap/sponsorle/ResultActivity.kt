package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils

class ResultActivity : AppCompatActivity() {
    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        val finalScore = TinyDB.getInt(this, "score", 0)
        if (finalScore > 0) {
            findViewById<TextView>(R.id.tv_count).text = "$finalScore"
        }
        val highScore = TinyDB.getInt(this, "high", 0)
        if (highScore > 0) {
            findViewById<TextView>(R.id.tv_High_Score).text = "$highScore"
        }
        findViewById<MaterialCardView>(R.id.cv_Retry).setOnClickListener {
            startActivity(Intent(this, PlayActivity::class.java))
            finish()
        }
        findViewById<MaterialCardView>(R.id.cv_2xscore).setOnClickListener {
            Utils.showLoadingPopUp(this)
            loadInterstitial()
        }
    }

    private fun loadInterstitial() {
        val adRequest = AdManagerAdRequest.Builder().build()

        RewardedAd.load(
            this,
            getString(R.string.rewarded),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    loadInterstitial()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedAd?.let { ad ->
                        Utils.dismissLoadingPopUp()
                        ad.show(this@ResultActivity) { rewardItem ->
                            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    val score=TinyDB.getInt(this@ResultActivity, "score", 0)*2
                                    TinyDB.saveInt(this@ResultActivity, "total_score", TinyDB.getInt(this@ResultActivity, "total_score", 0)-TinyDB.getInt(this@ResultActivity, "score", 0) + score)
                                    val heightScore = TinyDB.getInt(this@ResultActivity, "high", 0)
                                    if (heightScore < score) {
                                        TinyDB.saveInt(this@ResultActivity, "high", score)
                                    }
                                    Toast.makeText(this@ResultActivity, "2x Score Added", Toast.LENGTH_SHORT)
                                        .show()
                                    finish()

                                }
                            }
                        }
                    } ?: run {
                        loadInterstitial()
                    }

                }
            })

    }


}