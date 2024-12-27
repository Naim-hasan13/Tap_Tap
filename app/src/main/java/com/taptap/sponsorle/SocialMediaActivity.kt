package com.taptap.sponsorle

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.kabir.moneytree.extrazz.videoplayyer
import com.taptap.sponsorle.databinding.ActivitySocialMediaBinding
import com.taptap.sponsorle.extrazz.AdmobX
import com.taptap.sponsorle.extrazz.Companions
import com.taptap.sponsorle.extrazz.SocialTaskModel
import com.taptap.sponsorle.extrazz.TinyDB
import com.taptap.sponsorle.extrazz.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class SocialMediaActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySocialMediaBinding
    var list = ArrayList<SocialTaskModel>()

    init {
        System.loadLibrary("keys")
    }

    var upload = ""
    external fun Hatbc(): String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        getSocialTasks()
        loadNativeAd()

    }
    private fun loadNativeAd() {
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(
            this,
            TinyDB.getString(this@SocialMediaActivity, "adx_native", "")!!.replace("-", ",")
        ).forNativeAd { nativeAd ->
            Log.d("AdMob", "Native Ad Loaded.")
            AdmobX.loadNativeMediaumX(
                this,
                binding.myTemplate,
                TinyDB.getString(this@SocialMediaActivity, "adx_native", "")!!.replace("-", ",")
            )
        }.build()

        adLoader.loadAd(AdManagerAdRequest.Builder().build())
    }
    fun getSocialTasks() {
        Utils.showLoadingPopUp(this)

        val deviceid: String = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID
        )
        val emails = Base64.getEncoder().encodeToString(deviceid.toByteArray())
        val appid = Base64.getEncoder().encodeToString("${Companions.APP_ID}".toByteArray())
        val url = "${Companions.siteUrl}get_ocr_joining_task.php?email=$emails&app_id=$appid"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonArrayRequest =
            object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
                Utils.dismissLoadingPopUp()

                if (response.length() > 0) {
                    list.clear()

                    for (i in 0 until response.length()) {
                        val dataObject = response.getJSONObject(i)

                        val offer_title = dataObject.getString("offer_title")
                        val offer_link = dataObject.getString("offer_link")
                        val offer_coin = dataObject.getString("offer_coin")
                        val complete = dataObject.getInt("complete")
                        val id = dataObject.getString("id")
                        setUpoffer(offer_title, offer_link, offer_coin, complete, id)
                        val historyRedeemModal =
                            SocialTaskModel(complete, offer_coin, id, offer_link, offer_title)
                        list.add(historyRedeemModal)
                    }


                } else {
                    binding.cvYt.visibility = View.GONE
                    binding.cvTg.visibility = View.GONE
                    binding.cvIg.visibility = View.GONE
                }
            }, Response.ErrorListener { error ->
                Utils.dismissLoadingPopUp()
                Toast.makeText(this, "Internet Slow !", Toast.LENGTH_SHORT).show()
            }) {}

        requestQueue.add(jsonArrayRequest)
    }

    private fun setUpoffer(
        offerTitle: String,
        offerLink: String,
        offerCoin: String,
        complete: Int,
        id: String
    ) {
        if (offerTitle.contains("YT")) {
            binding.cvYt.visibility = View.VISIBLE

            if (complete == 1) {
                binding.tvYtStart.text = "CLAIMED"
            } else {
                binding.cvYtStart.setOnClickListener {
                    it.visibility = View.GONE
                    binding.cvYtUpload.visibility = View.VISIBLE
                    Utils.openUrl(this, offerLink)
                }
                binding.cvYtUpload.setOnClickListener {
                    ImagePicker.with(this).galleryOnly().start()
                    upload = id
                }
            }
        } else if (offerTitle.contains("TG")) {
            binding.cvTg.visibility = View.VISIBLE
            if (complete == 1) {
                binding.tvTgStart.text = "CLAIMED"
            } else {
                binding.cvTgStart.setOnClickListener {
                    it.visibility = View.GONE
                    binding.cvTgUpload.visibility = View.VISIBLE
                    Utils.openUrl(this, offerLink)
                }
                binding.cvTgUpload.setOnClickListener {
                    ImagePicker.with(this).galleryOnly().start()
                    upload = id
                }
            }
        } else if (offerTitle.contains("IG")) {
            binding.cvIg.visibility = View.VISIBLE
            if (complete == 1) {
                binding.tvIgStart.text = "CLAIMED"
            } else {
                binding.cvIgStart.setOnClickListener {
                    it.visibility = View.GONE
                    binding.cvIgUpload.visibility = View.VISIBLE
                    Utils.openUrl(this, offerLink)
                }
                binding.cvIgUpload.setOnClickListener {
                    ImagePicker.with(this).galleryOnly().start()
                    upload = id
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!
            Utils.showLoadingPopUp(this)
            performOCR(uri, upload)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performOCR(uri: Uri, offer_id: String) {
        try {
            val image: InputImage = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text
                    val deviceid =
                        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                    val time = System.currentTimeMillis()

                    val url2 = "${Companions.siteUrl}verify_ocr_joining_task.php"
                    val emails = Settings.Secure.getString(
                        contentResolver, Settings.Secure.ANDROID_ID
                    )
                    val queue1: RequestQueue = Volley.newRequestQueue(this)

                    val stringRequest = object : StringRequest(Method.POST, url2, { response ->
                        val ytes = Base64.getDecoder().decode(response)
                        val res = String(ytes, Charsets.UTF_8)
                        Utils.dismissLoadingPopUp()

                        if (res.contains(",")) {
                            val alldata = res.trim().split(",")
                            Toast.makeText(this, alldata[0], Toast.LENGTH_SHORT).show()
                            TinyDB.saveString(this, "balance", alldata[1])

                       list.forEach {
                           if (it.id==offer_id){
                               setUpoffer(it.offer_title, it.offer_link, it.offer_coin, 1, it.id)
                           }else{
                               setUpoffer(it.offer_title, it.offer_link, it.offer_coin, 0, it.id)
                           }
                       }
                        } else {
                            Toast.makeText(this, res, Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        Utils.dismissLoadingPopUp()
                        Toast.makeText(this, "Internet Slow: $error", Toast.LENGTH_SHORT).show()
                    }) {
                        override fun getParams(): Map<String, String> {
                            val params = mutableMapOf<String, String>()
                            val dbit32 = videoplayyer.encrypt(deviceid, Hatbc()).toString()
                            val tbit32 = videoplayyer.encrypt(time.toString(), Hatbc()).toString()
                            val emailsEnc =
                                videoplayyer.encrypt(emails.toString(), Hatbc()).toString()
                            val ocr =
                                videoplayyer.encrypt(recognizedText.toString(), Hatbc()).toString()
                            val offerIdEnc =
                                videoplayyer.encrypt(offer_id.toString(), Hatbc()).toString()

                            params["dase"] = URLEncoder.encode(
                                Base64.getEncoder().encodeToString(
                                    Json.encodeToString(
                                        mapOf(
                                            "deijvfijvmfhvfvhfbhbchbfybebd" to Base64.getEncoder()
                                                .encodeToString(dbit32.toByteArray()),
                                            "waofhfuisgdtdrefssfgsgsgdhddgd" to Base64.getEncoder()
                                                .encodeToString(tbit32.toByteArray()),
                                            "fdvbdfbhbrthyjsafewwt5yt5" to Base64.getEncoder()
                                                .encodeToString(emailsEnc.toByteArray()),
                                            "geugeubvjbvrugerugcceectgtg" to Base64.getEncoder()
                                                .encodeToString(ocr.toByteArray()),
                                            "gheghreghggnerg7ebvdfvdufgeurg" to Base64.getEncoder()
                                                .encodeToString(offerIdEnc.toByteArray())
                                        )
                                    ).toByteArray()
                                ), StandardCharsets.UTF_8.toString()
                            )
                            params["app_id"] = Base64.getEncoder().encodeToString(
                                Companions.APP_ID.toString().toByteArray()
                            )
                            return params
                        }
                    }

                    queue1.add(stringRequest)
                }
                .addOnFailureListener { e ->
                    Utils.dismissLoadingPopUp()
                    Toast.makeText(this, "OCR Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

}