package com.taptap.sponsorle

import android.content.Intent
import android.media.MediaPlayer
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
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.taptap.sponsorle.databinding.ActivityCreditedBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils
import kotlin.random.Random

class CreditedActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreditedBinding
    lateinit var adRequest: AdManagerAdRequest
    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreditedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        adRequest = AdManagerAdRequest.Builder().build()

        binding.tvBallence.text = TinyDB.getString(this, "coin_added", "")
        binding.cvAddToWallet.setOnClickListener {
            binding.cvAddToWallet.visibility=View.GONE
            binding.outerCardView.visibility=View.VISIBLE
            binding.outerCardView.post {
            animateProgress(binding.innerCardView, binding.outerCardView.width, Random.nextLong(3000,4500))
            }
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.coin)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
        loadNativeAd()
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
                    Utils.showLoadingPopUp(this@CreditedActivity)
                    loadInterstitial()
                }
            }
        })
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Click on add to wallet", Toast.LENGTH_SHORT).show()
    }

    private fun loadNativeAd() {
        binding.bannerAdView.loadAd(adRequest)
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@CreditedActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@CreditedActivity, "adx_native", "")!!.replace("-", ",")
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
                                startActivity(
                                    Intent(
                                        this@CreditedActivity,
                                        Add_WalletActivity::class.java
                                    )
                                )
                                finish()

                            }

                        }
                    mAdManagerInterstitialAd?.show(this@CreditedActivity)

                }
            }
        )
    }

}