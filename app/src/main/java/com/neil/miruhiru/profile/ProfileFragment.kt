package com.neil.miruhiru.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.databinding.FragmentProfileBinding
import timber.log.Timber


class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.likeChallenge.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalLikeChallengeFragment())
        }
        binding.joinChallenge.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalJoinFragment())
        }
        binding.notification.setOnClickListener {

        }
        binding.settings.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalVerifyDetailFragment())
        }
        binding.signOutButton.setOnClickListener {
            viewModel.signOut()
        }
        val challengeAdapter = ProfileChallengeAdapter { position ->
            val completedEventReversed = UserManager.user.completedEvents.reversed()
            this.findNavController().navigate(NavGraphDirections.actionGlobalLogFragment(completedEventReversed[position]))
        }
        binding.completedChallengeRecycler.adapter = challengeAdapter

        // observe user live data to make sure user data load success before setup profile info
        UserManager.userLiveData.observe(viewLifecycleOwner, Observer {

            Timber.i("user $it")
            if (it.completedEvents.isNotEmpty()) {
                viewModel.completedList.observe(viewLifecycleOwner, Observer { completedChallenges ->
                    challengeAdapter.submitList(completedChallenges)
                    challengeAdapter.notifyItemRangeChanged(0, challengeAdapter.itemCount)
                    binding.profileCompletedChallenges.text = "完成的挑數量：" + "${completedChallenges.size}"
                })
            }
            if (it.name.isNotEmpty()) {
                Glide.with(binding.profileIcon.context).load(UserManager.user.icon).circleCrop().apply(
                    RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
                ).into(binding.profileIcon)
                binding.profileName.text = "名稱：" + UserManager.user.name
            }
        })

        // observe sign out and navigate to sign in fragment
        viewModel.navigateToSignInFragment.observe(viewLifecycleOwner, Observer { signOut ->
            if (signOut) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalSignInFragment())
                viewModel.navigateToSignInFragmentCompleted()
            }
        })




        return binding.root
    }

}