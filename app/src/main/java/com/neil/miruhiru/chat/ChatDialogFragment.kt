package com.neil.miruhiru.chat

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.challengetype.ChallengeTypeViewModel
import com.neil.miruhiru.data.Message
import com.neil.miruhiru.databinding.FragmetChatDialogBinding
import kotlinx.coroutines.*

class ChatDialogFragment : DialogFragment() {

    private lateinit var binding: FragmetChatDialogBinding
    private lateinit var dialog: AlertDialog
    private val viewModel: ChatDialogViewModel by lazy {
        ViewModelProvider(this).get(ChatDialogViewModel::class.java)
    }
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmetChatDialogBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
        val messageAdapter = MessageAdapter(viewModel)
        binding.messageRecycler.adapter = messageAdapter
        val memberAdapter = ChatMemberAdapter()
        binding.memberRecycler.adapter = memberAdapter
        binding.memberRecycler.addItemDecoration(MemberItemDecorator(-20))

        binding.sendIcon.setOnClickListener {
            if (binding.editTextMessage.text.isNotEmpty()) {
                viewModel.sendMessages(binding.editTextMessage.text.toString())
                binding.editTextMessage.text.clear()
            }
        }
        binding.closeIcon.setOnClickListener {
            this.findNavController().navigateUp()
        }
        binding.removePersonIcon.setOnClickListener {
            kickUser()
        }

        // observe message and update
        viewModel.messageList.observe(this, Observer {
            if (it.isNotEmpty()) {
                UserManager.readMessages = it.size
                messageAdapter.submitList(it)
                messageAdapter.notifyDataSetChanged()
                binding.messageRecycler.scrollToPosition(messageAdapter.itemCount - 1)

                scope.launch {
                    delay(100)
                    binding.messageRecycler.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        })
        // observe load user and display user icon
        viewModel.memberIcons.observe(this, Observer {
            memberAdapter.submitList(it)
        })

        // Main user can kick other users
        viewModel.isMainUser.observe(this, Observer { isMainUser ->
            if (isMainUser) {
                binding.removePersonIcon.visibility = View.VISIBLE
            }
        })

        return dialog
    }

    private fun kickUser() {

        val db = Firebase.firestore

        val arrayAdapter = ArrayAdapter<String>(requireContext(),
            android.R.layout.select_dialog_singlechoice)
        for (user in viewModel.memberList) {
            if (user.id != UserManager.userId) {
                arrayAdapter.add(user.name)
            }
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("選擇要移除的使用者")
        builder.setSingleChoiceItems(arrayAdapter, -1) { dialog, which ->
            Toast.makeText(requireContext(), "移除 ${viewModel.event?.members?.get(which + 1)}", Toast.LENGTH_SHORT).show()
            viewModel.event?.members?.get(which + 1)?.let { viewModel.kick(it) }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

}