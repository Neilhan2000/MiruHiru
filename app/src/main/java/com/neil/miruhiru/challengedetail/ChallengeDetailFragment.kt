package com.neil.miruhiru.challengedetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentChallengeDetailBinding


/**
 * A simple [Fragment] subclass.
 * Use the [ChallengeDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChallengeDetailFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChallengeDetailBinding.inflate(inflater, container, false)






        return binding.root
    }


}