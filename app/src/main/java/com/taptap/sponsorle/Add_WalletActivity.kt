package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.taptap.sponsorle.databinding.ActivityAddWalletBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils
import kotlin.random.Random

class Add_WalletActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddWalletBinding
    lateinit var adRequest: AdManagerAdRequest
    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        MobileAds.initialize(this) {

        }
        adRequest = AdManagerAdRequest.Builder().build()
        loadNativeAd()
        binding.tvContinue.setOnClickListener {
            binding.tvContinue.visibility= View.GONE
            binding.outerCardView.visibility= View.VISIBLE
            binding.outerCardView.post {
                animateProgress(binding.innerCardView, binding.outerCardView.width, Random.nextLong(4000,10000))
            }
        }
    }
    private fun animateProgress(
        cardView: MaterialCardView,
        finalWidth: Int,
        duration: Long,
    ) {
        val startWidth = 0
        val layoutParams = cardView.layoutParams as ViewGroup.LayoutParams

        val handler = Handler(Looper.getMainLooper())
        val startTime = System.currentTimeMillis()

        handler.post(object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                val progress = elapsedTime.toFloat() / duration
                layoutParams.width = (startWidth + (finalWidth * progress)).toInt()
                cardView.layoutParams = layoutParams

                if (progress < 1.0f) {
                    handler.postDelayed(this, 16)
                } else {
                    Utils.showLoadingPopUp(this@Add_WalletActivity)
                    loadInterstitial()
                }
            }
        })
    }
    override fun onBackPressed() {
        Toast.makeText(this, "Click on continue", Toast.LENGTH_SHORT).show()
    }

    private fun loadNativeAd() {
        binding.bannerAdView.loadAd(adRequest)
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@Add_WalletActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@Add_WalletActivity, "adx_native", "")!!.replace("-", ",")
            )
        }.build()

        adLoader.loadAd(adRequest)
    }

    private fun loadInterstitial() {
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

                                finish()

                            }

                        }
                    mAdManagerInterstitialAd?.show(this@Add_WalletActivity)

                }
            }
        )
    }

}
