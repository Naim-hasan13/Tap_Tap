package com.taptap.sponsorle

import android.os.Bundle
import android.util.Log
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
import com.taptap.sponsorle.databinding.ActivitySuccessfullyWithdrawnBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils

class SuccessfullyWithdrawnActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuccessfullyWithdrawnBinding
    lateinit var adRequest: AdManagerAdRequest
    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessfullyWithdrawnBinding.inflate(layoutInflater)
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
            Utils.showLoadingPopUp(this)
            loadInterstitial()
        }
    }
    override fun onBackPressed() {
        Toast.makeText(this, "Click on continue", Toast.LENGTH_SHORT).show()
    }

    private fun loadNativeAd() {
        binding.bannerAdView.loadAd(adRequest)
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@SuccessfullyWithdrawnActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@SuccessfullyWithdrawnActivity, "adx_native", "")!!.replace("-", ",")
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
                    mAdManagerInterstitialAd?.show(this@SuccessfullyWithdrawnActivity)

                }
            }
        )
    }


}