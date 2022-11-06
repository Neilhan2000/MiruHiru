package com.neil.miruhiru.taskdetail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.data.LocationInfo
import com.neil.miruhiru.databinding.FragmentTaskDialogBinding

class TaskDialogFragment : DialogFragment() {

    lateinit var binding: FragmentTaskDialogBinding
    private lateinit var locationInfo: LocationInfo
    private lateinit var dialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentTaskDialogBinding.inflate(LayoutInflater.from(context))
        locationInfo = TaskDetailFragmentArgs.fromBundle(requireArguments()).locationInfo
        setupScreen()
        binding.answerButton2.setOnClickListener {
            submitAnswer(binding.editTextAnswer.text.toString())
        }











        return dialog
    }

    private fun setupScreen() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_border)
        binding.question.text = locationInfo.question
    }

    private fun isAnswerCorrect(answer: String): Boolean {
        return answer == locationInfo.answer
    }

    private fun submitAnswer(answer: String) {
        if (isAnswerCorrect(answer)) {
            this.findNavController().navigate(NavGraphDirections.actionGlobalTaskSuccessFragment())
        } else if (answer.isEmpty()) {
            Toast.makeText(requireContext(), "你還沒有輸入歐歐歐", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "不是這個答案歐，再試試看八", Toast.LENGTH_SHORT).show()
        }
    }
}