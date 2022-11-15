package com.neil.miruhiru.custom.item

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentMyCustomBinding

class MyCustomFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMyCustomBinding.inflate(inflater, container, false)





        return binding.root
    }

}