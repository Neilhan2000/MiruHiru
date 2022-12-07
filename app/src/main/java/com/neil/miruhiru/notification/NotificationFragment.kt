package com.neil.miruhiru.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.custom.item.MyCustomViewModel
import com.neil.miruhiru.data.Notification
import com.neil.miruhiru.databinding.FragmentNotificationBinding
import com.neil.miruhiru.network.LoadingStatus

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private val viewModel: NotificationViewModel by lazy {
        ViewModelProvider(this).get(NotificationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        val notificationAdapter = NotificationAdapter()
        binding.notificationRecycler.adapter = notificationAdapter
        viewModel.notificationList.observe(viewLifecycleOwner, Observer {
            notificationAdapter.submitList(it)
            UserManager.readNotifications = it.size
            if (it.size == 0) {
                binding.lottieAnimationView.visibility = View.VISIBLE
                binding.hint.visibility = View.VISIBLE
            }
        })
        viewModel.detectNotifications()

        // observe loading status and show progress bar
        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                LoadingStatus.LOADING -> {
                    MainActivity.getInstanceFromMainActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.VISIBLE
                }
                LoadingStatus.DONE -> {
                    MainActivity.getInstanceFromMainActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.GONE
                }
                LoadingStatus.ERROR -> {
                    MainActivity.getInstanceFromMainActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.GONE
                    Toast.makeText(requireContext(), "loading error", Toast.LENGTH_SHORT).show()
                }
            }
        })


        return binding.root
    }
}