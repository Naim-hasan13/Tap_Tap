package com.taptap.sponsorle

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.kabir.moneytree.extrazz.videoplayyer
import com.taptap.sponsorle.databinding.ActivitySpinBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.Companions
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils
import com.taptap.sponsorle.extrazz.Utils.makePositive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Random

class SpinActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpinBinding
    lateinit var adRequest: AdManagerAdRequest
    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        adRequest = AdManagerAdRequest.Builder().build()
        loadNativeAd()
        binding.tvSpin.setOnClickListener {
            val random = Random()
            val duration = 3000 + random.nextInt(1001) // Random duration between 3000 and 4000 ms

            val rotationAnimator = ObjectAnimator.ofFloat(binding.image, "rotation", 0f, 1080f)
            rotationAnimator.duration = duration.toLong()

            rotationAnimator.repeatCount = ObjectAnimator.INFINITE  // Infinite looping of rotation
            rotationAnimator.interpolator = null  // Linear interpolation
            rotationAnimator.start()

            Handler(Looper.getMainLooper()).postDelayed({
                rotationAnimator.cancel() // Stop the rotation after the set duration
                Utils.showLoadingPopUp(this)
                loadInterstitial()

            }, duration.toLong())
        }

    }

    private fun loadNativeAd() {
        binding.bannerAdView.loadAd(adRequest)
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@SpinActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@SpinActivity, "adx_native", "")!!.replace("-", ",")
            )
        }.build()

        adLoader.loadAd(adRequest)
    }

    private fun addPoint() {
        Utils.showLoadingPopUp(this)

        val deviceid: String = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url3 = "${Companions.siteUrl}play_point_custom.php"
        val email = TinyDB.getString(this, "email", "")

        val queue3: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.POST, url3, { response ->

                val yes = Base64.getDecoder().decode(response)
                val res = String(yes, Charsets.UTF_8)
                Utils.dismissLoadingPopUp()

                if (res.contains(",")) {
                    val alldata = res.trim().split(",")
                    TinyDB.saveString(this, "spin_limit", alldata[2])
                    val oldBalance = TinyDB.getString(this, "balance", "0").toString().toInt()
                    val difference = (oldBalance - alldata[1].toInt()).makePositive

                    TinyDB.saveString(this, "balance", alldata[1])
                    Utils.dismissLoadingPopUp()
                    TinyDB.saveString(this, "coin_added", difference.toString())

                    startActivity(Intent(this, CreditedActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
                }

            }, { error ->
                Utils.dismissLoadingPopUp()
                Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
                // requireActivity().finish()
            }) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()

                    val dbit32 = videoplayyer.encrypt(deviceid, Hatbc()).toString()
                    val tbit32 = videoplayyer.encrypt(time.toString(), Hatbc()).toString()
                    val email = videoplayyer.encrypt(email.toString(), Hatbc()).toString()
                    val code = videoplayyer.encrypt("s".toString(), Hatbc()).toString()

                    val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                    val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                    val email64 = Base64.getEncoder().encodeToString(email.toByteArray())
                    val code64 = Base64.getEncoder().encodeToString(code.toByteArray())

                    val encodemap: MutableMap<String, String> = HashMap()
                    encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                    encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
                    encodemap["fdvbdfbhbrthyjsafewwt5yt5"] = email64
                    encodemap["cfgcfxxghggujrzeawsrthyuyikhikfu"] = code64

                    val jason = Json.encodeToString(encodemap)

                    val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                    val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                    params["dase"] = final

                    val encodedAppID = Base64.getEncoder()
                        .encodeToString(
                            Companions.APP_ID.toString().toByteArray()
                        )
                    params["app_id"] = encodedAppID

                    return params
                }
            }

        queue3.add(stringRequest)
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

                                addPoint()
                            }

                        }
                    mAdManagerInterstitialAd?.show(this@SpinActivity)

                }
            }
        )
    }

}