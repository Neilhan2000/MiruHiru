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
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.data.User
import com.neil.miruhiru.databinding.FragmentInviteBinding

class InviteFragment : Fragment() {

    private lateinit var binding: FragmentInviteBinding
    private lateinit var eventId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInviteBinding.inflate(inflater, container, false)

        setupScreen()



        return binding.root
    }


    private fun setupScreen() {
        generateEventId()
        generateQrcode()
        val userList = listOf(User(), User())
        val userAdapter = UserAdapter()
        binding.userRecycler.adapter = userAdapter
        userAdapter.submitList(userList)

        binding.copyButton.setOnClickListener {
            val clipboardManager = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied String", binding.challengeKey.text)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "複製到剪貼簿成功", Toast.LENGTH_SHORT).show()
        }

        binding.startChallengeButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
        }
    }

    private fun generateQrcode() {
        val key = binding.challengeKey.text.toString().trim()
        val writer = MultiFormatWriter()
        val matrix = writer.encode(key, BarcodeFormat.QR_CODE, 800, 800)
        val encoder = BarcodeEncoder()
        val bitmap = encoder.createBitmap(matrix)
        binding.qrCode.setImageBitmap(bitmap)
    }
    private fun generateEventId() {
        eventId = getRandomString()
        binding.challengeKey.text = eventId
    }
    private fun getRandomString() : String {
        return java.util.UUID.randomUUID().toString()
    }

}