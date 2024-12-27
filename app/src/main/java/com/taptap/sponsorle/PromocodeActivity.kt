package com.taptap.sponsorle

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.kabir.moneytree.extrazz.videoplayyer
import com.taptap.sponsorle.databinding.ActivityPromocodeBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.Companions
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils
import com.taptap.sponsorle.extrazz.Utils.makePositive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PromocodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromocodeBinding
    private var redeemCode = ""
    lateinit var adRequest: AdManagerAdRequest

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromocodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        binding.cvSubmit.setOnClickListener {
            redeemCode = binding.etPromocodeCode.text.toString()
            if (redeemCode.isNotEmpty()) {
                applyPromoCode()
            } else {
                binding.etPromocodeCode.error = "Enter Promocode"
            }
        }
        adRequest = AdManagerAdRequest.Builder().build()
        loadNativeAd()
        binding.cvGetRedeeem.setOnClickListener {
            Utils.openUrl(this, TinyDB.getString(this, "telegram_link", "")!!)
        }
    }

    private fun loadNativeAd() {
        binding.bannerAdView.loadAd(adRequest)
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@PromocodeActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@PromocodeActivity, "adx_native", "")!!.replace("-", ",")
            )
        }.build()

        adLoader.loadAd(adRequest)
    }

    private fun applyPromoCode() {
        Utils.showLoadingPopUp(this)
        val deviceid: String = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url3 = "${Companions.siteUrl}apply_promocode.php"
        val email = TinyDB.getString(this, "phone", "")


        val queue3: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.POST, url3, { response ->

                val yes = java.util.Base64.getDecoder().decode(response)
                val res = String(yes, Charsets.UTF_8)
                Utils.dismissLoadingPopUp()

                if (res.contains(",")) {
                    val alldata = res.trim().split(",")
                    val oldBalance = TinyDB.getString(this, "balance", "0").toString().toInt()
                    val difference = (oldBalance - alldata[1].toInt()).makePositive
                    TinyDB.saveString(this, "balance", alldata[1])
                    Toast.makeText(this, alldata[0], Toast.LENGTH_SHORT).show()
                    TinyDB.saveString(this, "coin_added", difference.toString())
                    startActivity(Intent(this, CreditedActivity::class.java))
                    Utils.dismissLoadingPopUp()
                    finish()
                } else {
                    Toast.makeText(this, res, Toast.LENGTH_LONG).show()
                }


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
                    val promo = videoplayyer.encrypt(redeemCode, Hatbc()).toString()

                    val den64 = java.util.Base64.getEncoder().encodeToString(dbit32.toByteArray())
                    val ten64 = java.util.Base64.getEncoder().encodeToString(tbit32.toByteArray())
                    val email64 = java.util.Base64.getEncoder().encodeToString(email.toByteArray())
                    val promo64 = java.util.Base64.getEncoder().encodeToString(promo.toByteArray())

                    val encodemap: MutableMap<String, String> = HashMap()
                    encodemap["deijvfijvmfhvfvhfbhbchbfybebddgb"] = den64
                    encodemap["waofhfuisgdtdrefssfgsgsgdhddgder"] = ten64
                    encodemap["fdvbdfbhbrthyjsafewwt5yt5tedgwcv"] = email64
                    encodemap["prvbdfbhbrthyjsafewwt5ydesfsverg"] = promo64

                    val jason = Json.encodeToString(encodemap)

                    val den264 = java.util.Base64.getEncoder().encodeToString(jason.toByteArray())

                    val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                    params["dase"] = final

                    val encodedAppID =
                        java.util.Base64.getEncoder()
                            .encodeToString(Companions.APP_ID.toString().toByteArray())
                    params["app_id"] = encodedAppID

                    return params
                }
            }

        queue3.add(stringRequest)
    }

}