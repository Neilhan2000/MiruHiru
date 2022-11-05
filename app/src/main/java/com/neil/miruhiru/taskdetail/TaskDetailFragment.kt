package com.neil.miruhiru.taskdetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentTaskDetailBinding

class TaskDetailFragment : Fragment() {

    private lateinit var binding: FragmentTaskDetailBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false)







        return binding.root
    }

}