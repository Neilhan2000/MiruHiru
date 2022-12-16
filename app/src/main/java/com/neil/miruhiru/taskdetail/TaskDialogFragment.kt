package com.neil.miruhiru.taskdetail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.LocationInfo
import com.neil.miruhiru.databinding.FragmentTaskDialogBinding
import com.neil.miruhiru.tasksuccess.TaskSAndLogDViewModel
import timber.log.Timber

class TaskDialogFragment : DialogFragment() {

    lateinit var binding: FragmentTaskDialogBinding
    private lateinit var locationInfo: LocationInfo
    private lateinit var dialog: AlertDialog
    private val viewModel: TaskSAndLogDViewModel by lazy {
        ViewModelProvider(this).get(TaskSAndLogDViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentTaskDialogBinding.inflate(LayoutInflater.from(context))
        locationInfo = TaskDetailFragmentArgs.fromBundle(requireArguments()).locationInfo
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_border)
        binding.question.text = locationInfo.question

        binding.answerButton2.setOnClickListener {
            submitAnswer(binding.editTextAnswer.text.toString())
        }
        // live data navigate
        viewModel.navigateToTaskFragment.observe(this, Observer { updateProgressSuccess ->
            if (updateProgressSuccess) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskSuccessFragment())
                viewModel.navigateToTaskFragmentCompleted()
            }
        })
        return dialog
    }

    private fun isAnswerCorrect(answer: String): Boolean {
        return answer == locationInfo.answer
    }

    private fun submitAnswer(answer: String) {
        if (isAnswerCorrect(answer) ||
            UserManager.userId == getString(R.string.my_gmail) ||
            UserManager.userId == getString(R.string.miru_gmail) ||
            UserManager.userId == getString(R.string.hiru_gmail)) {

            Timber.i("current event stage ${viewModel.currentStage}, stage${viewModel.stageNumber}")
            viewModel.updateProgress()
        } else if (answer.isEmpty()) {

            Toast.makeText(requireContext(), getString(R.string.no_input), Toast.LENGTH_SHORT).show()
        } else {

            Toast.makeText(requireContext(), getString(R.string.not_correct_try_again), Toast.LENGTH_SHORT).show()
        }
    }
}