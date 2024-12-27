package com.taptap.sponsorle

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.taptap.sponsorle.databinding.ActivityRedeemHistoryBinding
import com.taptap.sponsorle.extrazz.Companions
import com.taptap.sponsorle.extrazz.HistoryAdapter
import com.taptap.sponsorle.extrazz.HistoryModal
import com.taptap.sponsorle.extrazz.TinyDB
import java.util.Base64

class Redeem_HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedeemHistoryBinding
    var History = ArrayList<HistoryModal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedeemHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = true
            insets
        }
        binding.tvBallance.text = TinyDB.getString(this, "balance", "0")
        getWithdrawalHistory()
    }

    fun getWithdrawalHistory() {
        binding.rvTotalHistory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val deviceid: String = Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        )
        val id = Base64.getEncoder().encodeToString("${Companions.APP_ID}".toByteArray())
        val url = "${Companions.siteUrl}get_withdrawal_history.php?email=$deviceid&app_id=$id"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonArrayRequest =
            object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
                binding.pb.visibility = View.GONE

                if (response.length() > 0) {
                    History.clear()

                    for (i in 0 until response.length()) {
                        val dataObject = response.getJSONObject(i)

                        val title = dataObject.getString("payment_title")
                        val amount = dataObject.getString("amount")
                        val date = dataObject.getString("date")
                        val status = dataObject.getString("status")
                        val historyRedeemModal = HistoryModal(title, amount, date, status)
                        History.add(historyRedeemModal)
                    }
                    val adapter = HistoryAdapter(this, History)
                    binding.rvTotalHistory.adapter = adapter
                } else {
                    binding.nodata.visibility = View.VISIBLE
                    binding.rvTotalHistory.visibility = View.GONE
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(this, "Internet Slow !", Toast.LENGTH_SHORT).show()
            }) {}

        requestQueue.add(jsonArrayRequest)
    }

}