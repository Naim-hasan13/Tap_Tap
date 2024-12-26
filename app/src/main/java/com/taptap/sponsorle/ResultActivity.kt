package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils

class ResultActivity : AppCompatActivity() {
    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null

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
            it.visibility= View.GONE
            loadInterstitial()
        }
    }

    private fun loadInterstitial() {
        val adRequest = AdManagerAdRequest.Builder().build()
        AdManagerInterstitialAd.load(
            this,
            getString(R.string.interstitial_id),
            adRequest,
            object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                    adError.toString().let { Log.d("AdMob", it) }
                    mAdManagerInterstitialAd = null
                    loadInterstitial()
                }

                override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                    Log.d("AdMob", "Ad was loaded.")
                    mAdManagerInterstitialAd = interstitialAd
                    Utils.dismissLoadingPopUp()
                    mAdManagerInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                // Reset the ad object and preload a new one
                                mAdManagerInterstitialAd = null
                               val score=TinyDB.getInt(this@ResultActivity, "score", 0)*2
                                TinyDB.saveInt(this@ResultActivity, "total_score", TinyDB.getInt(this@ResultActivity, "total_score", 0) + score)
                                val heightScore = TinyDB.getInt(this@ResultActivity, "high", 0)
                                if (heightScore < score) {
                                    TinyDB.saveInt(this@ResultActivity, "high", score)
                                }
                            }

                        }
                    mAdManagerInterstitialAd?.show(this@ResultActivity)

                }
            }
        )
    }


}