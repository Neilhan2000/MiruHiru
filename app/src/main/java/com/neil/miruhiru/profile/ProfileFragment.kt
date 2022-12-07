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
        var lastClickTime = 0L
        var firstClickTime = 0L
        var count = 0
        binding.settings.setOnClickListener {
            if (firstClickTime == 0L) {
                firstClickTime = Timestamp.now().seconds
            }
            count ++
            Timber.i("count $count")
            if (count == 5 && UserManager.userId == "tsaichenghan999@gmail.com") {
                lastClickTime = Timestamp.now().seconds
                if (lastClickTime - firstClickTime < 2) {
                    this.findNavController().navigate(NavGraphDirections.actionGlobalVerifyFragment())
                } else {
                    count = 0
                    firstClickTime = 0L
                }
                Timber.i("judge")
                Timber.i("interval ${lastClickTime - firstClickTime}")
                Timber.i("judge count $count")
            }
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
                binding.profileCreateChallenges.text = "完成的挑戰數量：" + "${UserManager.user.publicChallenges}"
                viewModel.cleanCompletedChallengeList()
                viewModel.loadCompletedChallenge()
            }
        })

        viewModel.completedList.observe(viewLifecycleOwner, Observer { completedChallenges ->
            challengeAdapter.submitList(completedChallenges)
            challengeAdapter.notifyItemRangeChanged(0, challengeAdapter.itemCount)
            binding.profileCompletedChallenges.text = "完成的挑數量：" + "${completedChallenges.size}"
        })

        // observe sign out and navigate to sign in fragment
        viewModel.navigateToSignInFragment.observe(viewLifecycleOwner, Observer { signOut ->
            if (signOut) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalSignInFragment())
                viewModel.navigateToSignInFragmentCompleted()
            }
        })



        val channel = NotificationChannel("tsai", "tsai", NotificationManager.IMPORTANCE_HIGH)
        val builder = Notification.Builder(requireContext(), "Day15")
        builder.setSmallIcon(R.drawable.anya_icon)
            .setContentTitle("Day15")
            .setContentText("Day15 Challenge")
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher_foreground))
            .setAutoCancel(true)
        val notification = builder.build()
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

//        binding.notification.setOnClickListener {
//            notificationManager.notify(0, notification)
//        }

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