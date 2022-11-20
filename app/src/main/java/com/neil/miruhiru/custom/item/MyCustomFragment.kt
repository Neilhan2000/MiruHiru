package com.neil.miruhiru.custom.item

import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentMyCustomBinding
import com.neil.miruhiru.task.TaskViewModel
import java.util.*

class MyCustomFragment : Fragment() {

    private val viewModel: MyCustomViewModel by lazy {
        ViewModelProvider(this).get(MyCustomViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMyCustomBinding.inflate(inflater, container, false)
        val customAdapter = MyCustomAdapter()
        binding.myCustomRecycler.adapter = customAdapter

        viewModel.myCustomList.observe(viewLifecycleOwner, Observer {
            customAdapter.submitList(it)
        })
        viewModel.loadMyCustom()







        return binding.root
    }

}