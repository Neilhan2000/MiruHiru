package com.neil.miruhiru.taskdetail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentTaskDialogBinding

class TaskDialogFragment : DialogFragment() {

    lateinit var binding: FragmentTaskDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentTaskDialogBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(requireActivity())

        builder.setView(binding.root)
        binding.root.setOnClickListener {
            requireActivity().finish()
        }
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_border)
        return dialog
    }
}