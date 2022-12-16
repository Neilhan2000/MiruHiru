package com.neil.miruhiru.profile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.databinding.FragmentProfileBinding
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber
import java.time.LocalDateTime


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
            (activity as MainActivity).cleanBadge()
            this.findNavController().navigate(NavGraphDirections.actionGlobalNotificationFragment())
        }

        if (UserManager.userId == getString(R.string.miru_gmail) ||
            UserManager.userId == getString(R.string.hiru_gmail) ||
            UserManager.userId == getString(R.string.my_gmail)) {
            binding.settings.visibility = View.VISIBLE
            binding.settings.setOnClickListener {
                this.findNavController().navigate(NavGraphDirections.actionGlobalVerifyFragment())
            }
        } else {
            binding.settings.visibility = View.GONE
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
            if (it.name.isNotEmpty()) {
                Glide.with(binding.profileIcon.context).load(UserManager.user.icon).circleCrop().apply(
                    RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
                ).into(binding.profileIcon)
                binding.profileName.text = "名稱：" + UserManager.user.name
                binding.profileCreateChallenges.text = "創造的挑戰數量：" + "${UserManager.user.publicChallenges}"
                viewModel.cleanCompletedChallengeList()
                viewModel.loadCompletedChallenge()
            }
        })

        viewModel.completedList.observe(viewLifecycleOwner, Observer { completedChallenges ->
            challengeAdapter.submitList(completedChallenges)
            challengeAdapter.notifyItemRangeChanged(0, challengeAdapter.itemCount)
            binding.profileCompletedChallenges.text = "完成的挑戰數量：" + "${completedChallenges.size}"
        })

        // observe sign out and navigate to sign in fragment
        viewModel.navigateToSignInFragment.observe(viewLifecycleOwner, Observer { signOut ->
            if (signOut) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalSignInFragment())
                viewModel.navigateToSignInFragmentCompleted()
            }
        })

        // notification icon
        if ((activity as MainActivity).isBadgeVisible()) {
            binding.notificationIcon.visibility = View.VISIBLE
        } else {
            binding.notificationIcon.visibility = View.GONE
        }

        // observe loading status and show progress bar
        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer { status ->
            Timber.i("status ${status.name}")
            when (status) {
                LoadingStatus.LOADING -> {
                    binding.progressBar2.visibility = View.VISIBLE
                }
                LoadingStatus.DONE -> {
                    binding.progressBar2.visibility = View.GONE
                }
                LoadingStatus.ERROR -> {
                    binding.progressBar2.visibility = View.GONE
                    Toast.makeText(requireContext(), "loading error", Toast.LENGTH_SHORT).show()
                }
            }
        })

        return binding.root
    }



}