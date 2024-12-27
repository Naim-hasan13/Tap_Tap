package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.material.card.MaterialCardView
import com.kabir.moneytree.extrazz.videoplayyer
import com.taptap.sponsorle.databinding.ActivityWithdrawalBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.Companions
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class WithdrawalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWithdrawalBinding
    lateinit var coin: String
    lateinit var type: String
    lateinit var title: String
    lateinit var adRequest: AdManagerAdRequest

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawalBinding.inflate(layoutInflater)
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
        coin = intent.getStringExtra("coin").toString()
        type = intent.getStringExtra("type").toString()
        title = intent.getStringExtra("title").toString()
        updateProgress(binding.cv2000FlamsInnerCardView, TinyDB.getString(this, "balance", "0")!!.toString().toInt(), coin.toInt(), binding.cv2000FlamsProgress)
        binding.tvBallance.text = coin + " Flams"
        binding.tvType.text = type
        binding.tvMoney.text = title
        binding.cvSubmit.setOnClickListener {
            if (binding.etEnterUPI.text.isEmpty()) {
                binding.etEnterUPI.error = "Enter UPI"
            } else {
                binding.llSpin.visibility = View.VISIBLE
                binding.withdrawalValue.text = coin
                binding.llDetails.visibility = View.GONE
            }
        }
        binding.cvContinue.setOnClickListener {
            redeemCoin(binding.etEnterUPI.text.toString())
        }

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
    private fun loadNativeAd() {
        binding.bannerAdView.loadAd(adRequest)
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@WithdrawalActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@WithdrawalActivity, "adx_native", "")!!.replace("-", ",")
            )
        }.build()

        adLoader.loadAd(adRequest)
    }

    private fun redeemCoin(text: String) {
        Utils.showLoadingPopUp(this)
        val deviceid: String = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url4 = "${Companions.siteUrl}redeem_point_2.php"
        val email = TinyDB.getString(this, "phone", "")

        val queue4: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.POST, url4, { response ->

                val yes = Base64.getDecoder().decode(response)
                val res = String(yes, Charsets.UTF_8)
                Utils.dismissLoadingPopUp()
                if (res.contains(",")) {
                    val alldata = res.trim().split(",")
                    Toast.makeText(this, alldata[0], Toast.LENGTH_SHORT).show()
                    TinyDB.saveString(this, "balance", alldata[1])

                    startActivity(Intent(this@WithdrawalActivity,SuccessfullyWithdrawnActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this, res, Toast.LENGTH_SHORT).show()

                }

                Utils.dismissLoadingPopUp()


            }, Response.ErrorListener { error ->
                Utils.dismissLoadingPopUp()
                Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
                // requireActivity().finish()
            }) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()

                    val dbit32 = videoplayyer.encrypt(deviceid, Hatbc()).toString()
                    val tbit32 = videoplayyer.encrypt(time.toString(), Hatbc()).toString()
                    val email = videoplayyer.encrypt(email.toString(), Hatbc()).toString()
                    val d32 = videoplayyer.encrypt(text.toString(), Hatbc()).toString()
                    val coin32 = videoplayyer.encrypt(coin.toString(), Hatbc()).toString()
                    val title32 =
                        videoplayyer.encrypt(type + " " + title.toString(), Hatbc()).toString()

                    val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                    val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                    val email64 = Base64.getEncoder().encodeToString(email.toByteArray())
                    val d64 = Base64.getEncoder().encodeToString(d32.toByteArray())
                    val bal64 = Base64.getEncoder().encodeToString(coin32.toByteArray())
                    val title64 = Base64.getEncoder().encodeToString(title32.toByteArray())

                    val encodemap: MutableMap<String, String> = HashMap()
                    encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                    encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
                    encodemap["fdvbdfbhbrthyjsafewwt5yt5"] = email64
                    encodemap["defsdfefsefwefwefewfwefvfvdfbdbd"] = d64
                    encodemap["balsdfefsefwefwefewfwefvfvdfbdbd"] = bal64
                    encodemap["namsdfefsefwefwefewfwefvfvdfbdbd"] = title64

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

        queue4.add(stringRequest)
    }

}