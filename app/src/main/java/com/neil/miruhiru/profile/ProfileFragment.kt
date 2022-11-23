package com.neil.miruhiru.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.databinding.FragmentProfileBinding
import timber.log.Timber


class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        Glide.with(binding.profileIcon.context).load(UserManager.user.icon).circleCrop().apply(
            RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
        ).into(binding.profileIcon)
        binding.profileName.text = "名稱：" + UserManager.user.name


        binding.likeChallenge.setOnClickListener {

        }
        binding.joinChallenge.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalJoinFragment())
        }
        binding.notification.setOnClickListener {

        }
        binding.settings.setOnClickListener {

        }




        return binding.root
    }

}