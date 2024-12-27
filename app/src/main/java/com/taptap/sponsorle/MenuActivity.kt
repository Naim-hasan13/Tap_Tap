package com.taptap.sponsorle

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.kabir.moneytree.extrazz.videoplayyer
import com.pubscale.sdkone.offerwall.OfferWall
import com.pubscale.sdkone.offerwall.OfferWallConfig
import com.pubscale.sdkone.offerwall.models.OfferWallInitListener
import com.pubscale.sdkone.offerwall.models.OfferWallListener
import com.pubscale.sdkone.offerwall.models.Reward
import com.pubscale.sdkone.offerwall.models.errors.InitError
import com.taptap.sponsorle.databinding.ActivityMenuBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.Companions
import com.taptap.sponsorle.extrazz.CustomAdLoader
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils
import com.taptap.sponsorle.extrazz.Utils.makePositive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    lateinit var adLoder: CustomAdLoader

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        adLoder = CustomAdLoader(
            context = this,
            siteAdsUrl = Companions.siteAdsUrl,
            uniqueAppId = Companions.EARNING_ADS_UNIQUE_APP_ID,
            onAdDismiss = {
                claimCoin()
            }
        )
        adLoder.loadAd()
        binding.cvEarningAds.setOnClickListener {
            adLoder.showAd()
        }
        val deviceid: String = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID
        )
        val offerWallConfig =
            OfferWallConfig.Builder(
                this,
                Companions.PUBSCALE_APP_ID
            )
                .setUniqueId(deviceid + "-" + Companions.APP_ID)
                .setFullscreenEnabled(true) //optional
                .build()

        OfferWall.init(offerWallConfig, object : OfferWallInitListener {
            override fun onInitSuccess() {
            }

            override fun onInitFailed(error: InitError) {
            }
        })
        binding.cvOfferWall.setOnClickListener {
            OfferWall.launch(this, offerWallListener)
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
            val intent = Intent(this, SocialMediaActivity::class.java)
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
        binding.cvRedeem.setOnClickListener {
            binding.llMenu.visibility = View.GONE
            binding.llRedeem.visibility = View.VISIBLE
            redeemProgress()
        }
        binding.cvExchangeTaps.setOnClickListener {
            showExchangePopup()
        }
        getUserValue()
        binding.cvHistory.setOnClickListener {
            val intent = Intent(this, Redeem_HistoryActivity::class.java)
            startActivity(intent)
        }
        binding.cv2000Flams.setOnClickListener {
            val intent = Intent(this, WithdrawalActivity::class.java)
            intent.putExtra("coin", "2000")
            intent.putExtra("type", "UPI")
            intent.putExtra("title", "₹10")
            startActivity(intent)
        }
        binding.cv2800Flams.setOnClickListener {
            val intent = Intent(this, WithdrawalActivity::class.java)
            intent.putExtra("coin", "2800")
            intent.putExtra("type", "UPI")
            intent.putExtra("title", "₹30")
            startActivity(intent)
        }
        binding.cv4000Flams.setOnClickListener {
            val intent = Intent(this, WithdrawalActivity::class.java)
            intent.putExtra("coin", "4000")
            intent.putExtra("type", "UPI")
            intent.putExtra("title", "₹50")
            startActivity(intent)
        }
        binding.cv7500Flams.setOnClickListener {
            val intent = Intent(this, WithdrawalActivity::class.java)
            intent.putExtra("coin", "7500")
            intent.putExtra("type", "UPI")
            intent.putExtra("title", "₹100")
            startActivity(intent)
        }
        binding.cvAmazon4000Flams.setOnClickListener {
            val intent = Intent(this, WithdrawalActivity::class.java)
            intent.putExtra("coin", "4000")
            intent.putExtra("type", "Amazon")
            intent.putExtra("title", "₹50")
            startActivity(intent)
        }
        binding.cvAmazon7500Flams.setOnClickListener {
            val intent = Intent(this, WithdrawalActivity::class.java)
            intent.putExtra("coin", "7500")
            intent.putExtra("type", "Amazon")
            intent.putExtra("title", "₹100")
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        updateLimits()
    }

    fun updateProgress(
        innerCardView: MaterialCardView,
        balance: Int,
        requiredCoins: Int,
        parentCardView: MaterialCardView
    ) {
        val parentLayoutParams = parentCardView.layoutParams as ViewGroup.LayoutParams
        val childLayoutParams = innerCardView.layoutParams as ViewGroup.LayoutParams

        parentCardView.post {
            val progressPercentage = balance.toFloat() / requiredCoins.toFloat()
            childLayoutParams.width = (parentLayoutParams.width * progressPercentage).toInt()
            innerCardView.layoutParams = childLayoutParams
        }
    }

    fun redeemProgress() {
        val balance = TinyDB.getString(this, "balance", "0")!!.toString().toInt()
        updateProgress(
            binding.cv2000FlamsInnerCardView,
            balance,
            2000,
            binding.cv2000FlamsProgress
        )
        updateProgress(
            binding.cv2800FlamsInnerCardView,
            balance, 2800, binding.cv2800FlamsProgress
        )

        updateProgress(
            binding.cv4000FlamsInnerCardView,
            balance, 4000, binding.cv4000FlamsProgress
        )
        updateProgress(
            binding.cv7500FlamsInnerCardView,
            balance, 7500, binding.cv7500FlamsProgress
        )
        updateProgress(
            binding.cvAmazon4000FlamsInnerCardView,
            balance,
            4000,
            binding.cvAmazon4000FlamsProgress
        )
        updateProgress(
            binding.cvAmazon7500FlamsInnerCardView,
            balance,
            7500,
            binding.cvAmazon7500FlamsProgress
        )


    }

    private fun claimCoin() {
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
                    TinyDB.saveString(this, "earning_ad_limit", alldata[2])
                    val oldBalance = TinyDB.getString(this, "balance", "0").toString().toInt()
                    val difference = (oldBalance - alldata[1].toInt()).makePositive

                    TinyDB.saveString(this, "balance", alldata[1])
                    Utils.dismissLoadingPopUp()
                    TinyDB.saveString(this, "coin_added", difference.toString())
                    startActivity(Intent(this, CreditedActivity::class.java))
                    adLoder.loadAd()
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
                    val code = videoplayyer.encrypt("ea".toString(), Hatbc()).toString()

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

    override fun onBackPressed() {
        if (binding.llRedeem.visibility == View.VISIBLE) {
            binding.llMenu.visibility = View.VISIBLE
            binding.llRedeem.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    fun getUserValue() {
        Utils.showLoadingPopUp(this)
        val deviceid: String = Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url2 = "${Companions.siteUrl}getuservalue.php"
        val email = TinyDB.getString(this, "email", "")

        val queue1: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.POST, url2, { response ->
                val ytes = Base64.getDecoder().decode(response)
                val res = String(ytes, Charsets.UTF_8)

                if (res.contains(",")) {
                    val alldata = res.trim().split(",")
                    TinyDB.saveString(this, "phone", alldata[0])
                    TinyDB.saveString(this, "maintenance", alldata[1])
                    TinyDB.saveString(this, "version", alldata[2])
                    TinyDB.saveString(this, "balance", alldata[3])
                    TinyDB.saveString(this, "earning_ad_limit", alldata[4])
                    TinyDB.saveString(this, "telegram_link", alldata[5])
                    TinyDB.saveString(this, "refer_code", alldata[6])
                    TinyDB.saveString(this, "sponsor_link", alldata[7])
                    TinyDB.saveString(this, "app_link", alldata[8])
                    TinyDB.saveString(this, "play_limit", alldata[9])
                    TinyDB.saveString(this, "show_ads_after_each", alldata[10])
                    TinyDB.saveString(this, "adx_app_id", alldata[11])
                    TinyDB.saveString(this, "adx_inter", alldata[12])
                    TinyDB.saveString(this, "adx_native", alldata[13])
                    TinyDB.saveString(this, "balance_exchange_rate", alldata[14])
                    TinyDB.saveString(this, "balance_withdrawal_limit", alldata[15])
                    TinyDB.saveString(this, "adx_banner", alldata[16])
                    TinyDB.saveString(this, "original_play_limit", alldata[17])
                    TinyDB.saveString(this, "adx_app_open", alldata[18])
                    TinyDB.saveString(this, "temp_balance", alldata[19])
                    TinyDB.saveString(this, "check_in_limit", alldata[20])
                    TinyDB.saveString(this, "check_in_original_limit", alldata[21])
                    TinyDB.saveString(this, "spin_limit", alldata[22])
                    TinyDB.saveString(this, "spin_original_limit", alldata[23])
                    TinyDB.saveString(this, "captcha_limit", alldata[24])
                    TinyDB.saveString(this, "captcha_original_limit", alldata[25])
                    TinyDB.saveString(this, "scratch_limit", alldata[26])
                    TinyDB.saveString(this, "scratch_original_limit", alldata[27])
                    TinyDB.saveString(this, "earning_ad_original_limit", alldata[28])
                    TinyDB.saveString(this, "total_registration", alldata[29])
                    TinyDB.saveString(this, "total_referral", alldata[30])






                    updateAdMobAppId(alldata[11])
                    updateLimits()

                    if (alldata[2].toInt() > Companions.APP_VERSION) {
                        showUpdatePopup()
                    } else if (alldata[1] == "1") {
                        showMaintaincePopup()
                    } else {
                        showJoinTgPopup()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        Utils.dismissLoadingPopUp()
                    }, 1500)

                } else {
                    Toast.makeText(this, res, Toast.LENGTH_LONG).show()
                    finish()
                }


            }, { _ ->
                Utils.dismissLoadingPopUp()
                Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
                finish()
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

                    val jason = Json.encodeToString(encodemap)

                    val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                    val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                    params["dase"] = final

                    val encodedAppID = Base64.getEncoder()
                        .encodeToString(Companions.APP_ID.toString().toByteArray())
                    params["app_id"] = encodedAppID

                    return params
                }
            }

        queue1.add(stringRequest)
    }

    private fun updateLimits() {
        binding.tvBalance.text = TinyDB.getString(this, "balance", "")!!
        binding.tvChackinProgress.text = "${TinyDB.getString(this, "check_in_limit", "0")!!} / ${
            TinyDB.getString(
                this,
                "check_in_original_limit",
                "10"
            )!!
        }"
        updateProgress(
            binding.cvChackInnerCardView,
            TinyDB.getString(this, "check_in_limit", "0")!!.toString().toInt(),
            TinyDB.getString(this, "check_in_original_limit", "10")!!.toInt(),
            binding.cvChackinProgress
        )
        binding.tvSpinProgress.text = "${TinyDB.getString(this, "spin_limit", "")!!} / ${
            TinyDB.getString(
                this,
                "spin_original_limit",
                "10"
            )!!
        }"
        updateProgress(
            binding.spinInnerCardView,
            TinyDB.getString(this, "spin_limit", "0")!!.toString().toInt(),
            TinyDB.getString(this, "spin_original_limit", "10")!!.toInt(),
            binding.cvSpinProgress
        )
        binding.tvCaptchaProgress.text = "${TinyDB.getString(this, "captcha_limit", "0")!!} / ${
            TinyDB.getString(
                this,
                "captcha_original_limit",
                ""
            )!!
        }"
        updateProgress(
            binding.captchaInnerCardView,
            TinyDB.getString(this, "captcha_limit", "0")!!.toString().toInt(),
            TinyDB.getString(this, "captcha_original_limit", "10")!!.toInt(),
            binding.cvCaptchaProgress
        )
        binding.tvScratchProgress.text = "${TinyDB.getString(this, "scratch_limit", "")!!} / ${
            TinyDB.getString(
                this,
                "scratch_original_limit",
                ""
            )!!
        }"
        updateProgress(
            binding.ScratchInnerCardView,
            TinyDB.getString(this, "scratch_limit", "0")!!.toString().toInt(),
            TinyDB.getString(this, "scratch_original_limit", "10")!!.toInt(),
            binding.cvScratchProgress
        )
        binding.tvEarningAdsProgress.text =
            "${TinyDB.getString(this, "earning_ad_limit", "")!!} / ${
                TinyDB.getString(
                    this,
                    "earning_ad_original_limit",
                    ""
                )!!
            }"
        updateProgress(
            binding.EarningAdsInnerCardView,
            TinyDB.getString(this, "earning_ad_limit", "0")!!.toString().toInt(),
            TinyDB.getString(this, "earning_ad_original_limit", "10")!!.toInt(),
            binding.cvEarningAdsProgress
        )


    }

    private fun showMaintaincePopup() {
        AlertDialog.Builder(this, R.style.updateDialogTheme).setView(R.layout.popup_maintaince)
            .setCancelable(false).create().apply {
                show()
                findViewById<MaterialCardView>(R.id.cv_okay)?.setOnClickListener {
                    Utils.openUrl(
                        this@MenuActivity,
                        TinyDB.getString(this@MenuActivity, "telegram_link", "0")!!
                    )
                }
            }
    }

    private fun showUpdatePopup() {
        AlertDialog.Builder(this, R.style.updateDialogTheme).setView(R.layout.popup_newupdate)
            .setCancelable(false).create().apply {
                show()
                findViewById<MaterialCardView>(R.id.cv_okay)?.setOnClickListener {
                    Utils.openUrl(
                        this@MenuActivity,
                        TinyDB.getString(this@MenuActivity, "app_link", "")!!
                    )
                }
            }

    }

    private fun showJoinTgPopup() {
        AlertDialog.Builder(this, R.style.updateDialogTheme).setView(R.layout.popup_join_telegram)
            .setCancelable(true).create().apply {
                show()
                findViewById<MaterialCardView>(R.id.cv_Join_Telegram)?.setOnClickListener {
                    Utils.openUrl(
                        this@MenuActivity,
                        TinyDB.getString(this@MenuActivity, "telegram_link", "")!!
                    )
                    dismiss()
                }
            }

    }

    private fun showExchangePopup() {
        AlertDialog.Builder(this, R.style.updateDialogTheme).setView(R.layout.popup_exchange_taps)
            .setCancelable(true).create().apply {
                show()
                findViewById<TextView>(R.id.tv_value)?.text =
                    TinyDB.getInt(this@MenuActivity, "total_score", 0).toString()
                findViewById<TextView>(R.id.cv_exchanged_coin)?.text =
                    (TinyDB.getInt(
                        this@MenuActivity,
                        "total_score",
                        0
                    ) / TinyDB.getString(this@MenuActivity, "balance_exchange_rate", "")!!
                        .toInt()).toString() + " Flams"
                findViewById<MaterialCardView>(R.id.cv_Exchange_Flams)?.setOnClickListener {
                    exchangePoint(TinyDB.getInt(this@MenuActivity, "total_score", 0).toString())
                    dismiss()
                }
                findViewById<MaterialCardView>(R.id.cv_close)?.setOnClickListener {
                    dismiss()
                }
            }

    }

    private fun updateAdMobAppId(adMobAppId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val applicationInfo = packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
                val metaData = applicationInfo.metaData

                if (metaData != null) {
                    metaData.putString("com.google.android.gms.ads.APPLICATION_ID", adMobAppId)
                    println("AdMob App ID updated: $adMobAppId")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            MobileAds.initialize(this@MenuActivity) {

            }
            loadInterstitial()
            loadNativeAd()
        }
    }

    private fun loadInterstitial() {
        val adRequest = AdManagerAdRequest.Builder().build()
        AdManagerInterstitialAd.load(
            this,
            TinyDB.getString(this@MenuActivity, "adx_inter", "")!!.replace("-", ","),
            adRequest,
            object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                    adError.toString().let { Log.d("AdMob", it) }
                    mAdManagerInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                    Log.d("AdMob", "Ad was loaded.")
                    mAdManagerInterstitialAd = interstitialAd
                }
            }
        )
    }

    private fun loadNativeAd() {
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@MenuActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@MenuActivity, "adx_native", "")!!.replace("-", ",")
            )
        }.build()

        adLoader.loadAd(AdManagerAdRequest.Builder().build())
    }

    private val offerWallListener = object : OfferWallListener {

        override fun onOfferWallShowed() {

        }

        override fun onOfferWallClosed() {

        }

        override fun onRewardClaimed(reward: Reward) {

        }

        override fun onFailed(message: String) {
            Toast.makeText(this@MenuActivity, "No Offers Available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exchangePoint(coin: String) {
        Utils.showLoadingPopUp(this)
        if (coin.isEmpty()) {
            Toast.makeText(this, "Enter Amount", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceid: String = Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url3 = "${Companions.siteUrl}exchange_point.php"
        val email = TinyDB.getString(this, "email", "")

        val queue3: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST, url3, { response ->

            val yes = Base64.getDecoder().decode(response)
            val res = String(yes, Charsets.UTF_8)

            if (res.contains(",")) {
                Utils.dismissLoadingPopUp()
                val alldata = res.trim().split(",")

                TinyDB.saveString(this, "balance", alldata[1])
                Toast.makeText(this, alldata[0], Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    Utils.dismissLoadingPopUp()
                    TinyDB.saveInt(this@MenuActivity, "total_score", 0)
                    updateLimits()

                }, 1000)

            } else {
                Toast.makeText(this, res, Toast.LENGTH_LONG).show()
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
                val upi32 = videoplayyer.encrypt(coin.toString(), Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                val email64 = Base64.getEncoder().encodeToString(email.toByteArray())
                val upi64 = Base64.getEncoder().encodeToString(upi32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
                encodemap["fdvbdfbhbrthyjsafewwt5yt5"] = email64
                encodemap["defsdfefsefwefwefewfwefvfvdfbdbd"] = upi64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final

                val encodedAppID = Base64.getEncoder().encodeToString(
                    Companions.APP_ID.toString().toByteArray()
                )
                params["app_id"] = encodedAppID

                return params
            }
        }

        queue3.add(stringRequest)


    }

}
