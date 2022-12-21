package com.example.glasswire.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.glasswire.R
import com.example.glasswire.fragments.DataFormat
import com.example.glasswire.models.AppUsageModel

class AppDataUsageAdapter(private val list: List<AppUsageModel>): RecyclerView.Adapter<AppDataUsageAdapter.AppUsageViewHolder>() {

    class AppUsageViewHolder(appView: View) : RecyclerView.ViewHolder(appView) {
        val icon: ImageView = appView.findViewById(R.id.app_icon)
        val appName: TextView = appView.findViewById(R.id.app_name)
        val sent: TextView = appView.findViewById(R.id.sent)
        val received: TextView = appView.findViewById(R.id.received)
        val total: TextView = appView.findViewById(R.id.total)
        val isSystemApp: TextView = appView.findViewById(R.id.is_system_app)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_app_usage_view, parent, false)

        return AppUsageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppUsageViewHolder, position: Int) {
        val appViewModel = list[position]

        appViewModel.apply {
            val (sent, received, total) = formatData(sent, received, DataFormat.Binary)

            holder.appName.text = appViewModel.packageName
            holder.sent.text = sent
            holder.received.text = received
            holder.total.text = total
            holder.icon.setImageDrawable(appViewModel.icon)
            holder.isSystemApp.text = appViewModel.systemApp.toString()
        }
    }

    override fun getItemCount(): Int = list.size

    /**
     * Formats the data
     */
    private fun formatData(sent: Long, received: Long, dataFormat: DataFormat): Array<String> {

        val divisor: Float = when(dataFormat) {
            DataFormat.Binary -> 1024f
            DataFormat.Decimal -> 1000f
        }

        val totalBytes = (sent + received) / divisor
        val sentBytes = sent / divisor
        val receivedBytes = received / divisor

        val totalMB = totalBytes / divisor

        val totalGB: Float
        val sentGB: Float
        val receivedGB: Float

        val sentMB: Float = sentBytes / divisor
        val receivedMB: Float = receivedBytes / divisor

        val sentData: String
        val receivedData: String
        val totalData: String
        if (totalMB > divisor) {
            totalGB = totalMB / divisor
            totalData = String.format("%.2f", totalGB) + " GB"
        } else {
            totalData = String.format("%.2f", totalMB) + " MB"
        }

        if (sentMB > divisor) {
            sentGB = sentMB / divisor
            sentData = String.format("%.2f", sentGB) + " GB"
        } else {
            sentData = String.format("%.2f", sentMB) + " MB"
        }
        if (receivedMB > divisor) {
            receivedGB = receivedMB / divisor
            receivedData = String.format("%.2f", receivedGB) + " GB"
        } else {
            receivedData = String.format("%.2f", receivedMB) + " MB"
        }

        return arrayOf(sentData, receivedData, totalData)
    }

}