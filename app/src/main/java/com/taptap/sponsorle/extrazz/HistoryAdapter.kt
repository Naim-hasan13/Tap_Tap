package com.taptap.sponsorle.extrazz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.material.card.MaterialCardView
import com.taptap.sponsorle.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryAdapter(
    private val context: Context,
    private val jsonArray: ArrayList<HistoryModal>
) :
    RecyclerView.Adapter<HistoryAdapter.PaymentViewHolder>() {

    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_transction_history, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val item = jsonArray[position]
        holder.type.text = item.title.split(" ")[0]
        holder.paymentTitleTextView.text = item.title.split(" ")[1]

        if (item.status.toInt() == 1) {

            holder.status.text = "Success"
            holder.innerCard.setCardBackgroundColor(context.getColor(R.color.success))
        } else if (item.status.toInt() == -1) {
            holder.status.text = "Refunded"
            holder.innerCard.setCardBackgroundColor(context.getColor(R.color.refunded))

        } else {
            holder.innerCard.setCardBackgroundColor(context.getColor(R.color.pending))

            holder.status.text = "Pending"
        }
    }


    override fun getItemCount(): Int {
        return jsonArray.size
    }

    fun updateTrans(updateTrans: ArrayList<HistoryModal>) {
        jsonArray.clear()
        jsonArray.addAll(updateTrans)
        notifyDataSetChanged()
    }

    fun formatDateTime(input: String): String {
        // Define the input format
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Parse the input date-time string
        val dateTime = LocalDateTime.parse(input, inputFormatter)

        // Define the output format
        val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a")

        // Format the date-time to the desired output format
        return dateTime.format(outputFormatter)
    }

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentTitleTextView: TextView = itemView.findViewById(R.id.tv_title)
        val status: TextView = itemView.findViewById(R.id.tv_status)
        val type: TextView = itemView.findViewById(R.id.tv_type)
        val innerCard: MaterialCardView = itemView.findViewById(R.id.cv_inner)
    }

}
