package com.neil.miruhiru.tasksuccess

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentLogDialogBinding


class LogDialogFragment : DialogFragment() {

    lateinit var binding: FragmentLogDialogBinding
    private lateinit var dialog: AlertDialog
    private lateinit var viewModel: TaskSAndLogDViewModel


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentLogDialogBinding.inflate(LayoutInflater.from(context))
        setupScreen()
        viewModel = ViewModelProvider(this).get(TaskSAndLogDViewModel::class.java)
        return dialog
    }

    private fun setupScreen() {

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_border)
        binding.logButton2.setOnClickListener {
            if (isInputValid()) {
                if (viewModel.currentStage < viewModel.stageNumber) {
                    viewModel.updateProgress()
                    this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
                } else {
                    viewModel.completeEvent()
                    this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeSuccessFragment())
                }
            }
        }
    }

    private fun isInputValid(): Boolean {
        return if (binding.editTextLog.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "至少要輸入文字歐歐歐", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }
}