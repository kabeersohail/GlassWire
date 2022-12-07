package com.example.glasswire.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.glasswire.adapters.AppDataUsageAdapter
import com.example.glasswire.databinding.FragmentRecyclerViewBinding
import com.example.glasswire.viewmodels.SharedViewModel

class RecyclerViewFragment : Fragment() {

    lateinit var fragmentRecyclerViewBinding: FragmentRecyclerViewBinding

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentRecyclerViewBinding = FragmentRecyclerViewBinding.inflate(inflater, container, false)
        return fragmentRecyclerViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appDataUsageRecyclerView: RecyclerView = fragmentRecyclerViewBinding.appDataUsageRecyclerView

        appDataUsageRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        sharedViewModel.appDataUsageList.observe(viewLifecycleOwner) { requiredList ->
            appDataUsageRecyclerView.adapter = AppDataUsageAdapter(requiredList)
        }
    }
}