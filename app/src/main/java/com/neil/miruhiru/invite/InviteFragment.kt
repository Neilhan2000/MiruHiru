package com.neil.miruhiru.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.databinding.FragmentInviteBinding

class InviteFragment : Fragment() {

    private lateinit var binding: FragmentInviteBinding
    private lateinit var eventId: String
    private val viewModel: InviteFragmentViewModel by lazy {
        ViewModelProvider(this).get(InviteFragmentViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInviteBinding.inflate(inflater, container, false)

        setupScreen()



        return binding.root
    }


    private fun setupScreen() {
        getEventId()
        generateQrcode()

        binding.copyButton.setOnClickListener {
            val clipboardManager = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied String", binding.challengeKey.text)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "複製到剪貼簿成功", Toast.LENGTH_SHORT).show()
        }

        // start event
        binding.startChallengeButton.setOnClickListener {
            viewModel.eventStart(eventId)
        }
        viewModel.navigateToTaskFragment.observe(viewLifecycleOwner, Observer { eventStart ->
            if (eventStart == true) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
                viewModel.navigateToTaskFragmentCompleted()
                //snapshot listen status
            }
        })

        // user recyclerview
        viewModel.detectUserJoin(eventId)
        viewModel.detectEventStart(eventId)

        val userAdapter = UserAdapter()
        binding.userRecycler.adapter = userAdapter
        viewModel.userList.observe(viewLifecycleOwner, Observer {
            userAdapter.submitList(it)
            // check if the user is main user (only main user can control event is started or not)
//            if (UserManager.user.id != it[0].id) {
//                changeButtonStatus()
//            }
        })
    }

    private fun generateQrcode() {
        val key = binding.challengeKey.text.toString().trim()
        val writer = MultiFormatWriter()
        val matrix = writer.encode(key, BarcodeFormat.QR_CODE, 800, 800)
        val encoder = BarcodeEncoder()
        val bitmap = encoder.createBitmap(matrix)
        binding.qrCode.setImageBitmap(bitmap)
    }
    private fun getEventId() {
        eventId = InviteFragmentArgs.fromBundle(requireArguments()).eventId
        binding.challengeKey.text = eventId
    }
    private fun changeButtonStatus() {
        binding.startChallengeButton.isEnabled = false
        binding.startChallengeButton.text = "等待挑戰開始"
        binding.startChallengeButton.setBackgroundResource(R.drawable.button_disable_border)
    }

}