package com.example.glasswire.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.glasswire.R
import com.example.glasswire.models.AppUsageModel

class AppDataUsageAdapter(private val list: List<AppUsageModel>): RecyclerView.Adapter<AppDataUsageAdapter.AppUsageViewHolder>() {

    class AppUsageViewHolder(appView: View) : RecyclerView.ViewHolder(appView) {
        val icon: ImageView = appView.findViewById(R.id.app_icon)
        val appName: TextView = appView.findViewById(R.id.app_name)
        val sent: TextView = appView.findViewById(R.id.sent)
        val received: TextView = appView.findViewById(R.id.received)
        val total: TextView = appView.findViewById(R.id.total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_app_usage_view, parent, false)

        return AppUsageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppUsageViewHolder, position: Int) {
        val appViewModel = list[position]

        holder.appName.text = appViewModel.packageName
        holder.sent.text = appViewModel.sent.toString()
        holder.received.text = appViewModel.received.toString()
        holder.total.text = appViewModel.total.toString()
        holder.icon.setImageDrawable(appViewModel.icon)
    }

    override fun getItemCount(): Int = list.size


}