package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.kabir.moneytree.extrazz.videoplayyer
import com.taptap.sponsorle.databinding.ActivityCheckInBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.Companions
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class CheckInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckInBinding
    private var rewardedAd: RewardedAd? = null
    lateinit var adRequest: AdManagerAdRequest

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckInBinding.inflate(layoutInflater)
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

        setupAddToWalletButton()
        binding.cvCheckin.setOnClickListener {
            Utils.showLoadingPopUp(this)
            loadRewardedAd()
        }
        loadNativeAd()
    }

    private fun loadNativeAd() {
        binding.bannerAdView.loadAd(adRequest)
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@CheckInActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@CheckInActivity, "adx_native", "")!!.replace("-", ",")
            )
        }.build()

        adLoader.loadAd(adRequest)
    }

    private fun loadRewardedAd() {
        RewardedAd.load(
            this,
            getString(R.string.rewarded),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    loadRewardedAd()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedAd?.let { ad ->
                        Utils.dismissLoadingPopUp()
                        ad.show(this@CheckInActivity, OnUserEarnedRewardListener { rewardItem ->
                            // Handle the reward.
                            val rewardAmount = rewardItem.amount
                            val rewardType = rewardItem.type

                            ad.fullScreenContentCallback=object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    addPoint()
                                }
                            }
                        })
                    } ?: run {
                        loadRewardedAd()
                    }

                }
            })
    }

    private fun addPoint() {

        val deviceid: String = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url3 = "${Companions.siteUrl}daily_check.php"
        val email = TinyDB.getString(this, "email", "")

        val queue3: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.POST, url3, { response ->

                val yes = Base64.getDecoder().decode(response)
                val res = String(yes, Charsets.UTF_8)

                if (res.contains(",")) {
                    Utils.dismissLoadingPopUp()
                    val alldata = res.trim().split(",")
                    TinyDB.saveString(this, "check_in_limit", alldata[2])
                    val oldBalance = TinyDB.getString(this, "balance", "")!!.toInt()
                    val difference = alldata[1].toInt() - oldBalance
                    TinyDB.saveString(this, "balance", alldata[1])

                    binding.tvBallence.text=difference.toString()
                    TinyDB.saveString(this,"coin_added",difference.toString())
//                    binding.llFlameCredit.visibility = View.VISIBLE
//                    binding.llCheckin.visibility = View.GONE
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

                    val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                    val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                    val email64 = Base64.getEncoder().encodeToString(email.toByteArray())

                    val encodemap: MutableMap<String, String> = HashMap()
                    encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                    encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
                    encodemap["fdvbdfbhbrthyjsafewwt5yt5"] = email64

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

    private fun setupAddToWalletButton() {
        binding.cvAddToWallet.setOnClickListener {
            // Navigate to Add Wallet Activity
            val intent = Intent(this, Add_WalletActivity::class.java)
            startActivity(intent)
        }
    }
}