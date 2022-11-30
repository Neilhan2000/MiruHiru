package com.neil.miruhiru.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.custom.item.MyCustomViewModel
import com.neil.miruhiru.data.Notification
import com.neil.miruhiru.databinding.FragmentNotificationBinding

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


        return binding.root
    }
}