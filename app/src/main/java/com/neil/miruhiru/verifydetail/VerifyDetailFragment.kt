package com.neil.miruhiru.verifydetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentVerifyDetailBinding
import timber.log.Timber

class VerifyDetailFragment : Fragment() {

    private lateinit var binding: FragmentVerifyDetailBinding
    private val viewModel: VerifyDetailViewModel by lazy {
        ViewModelProvider(this).get(VerifyDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerifyDetailBinding.inflate(inflater, container, false)
        val challengeId = VerifyDetailFragmentArgs.fromBundle(requireArguments()).challengeId

        val taskAdapter = VerifyTaskAdapter()
        binding.taskRecycler.adapter = taskAdapter
        viewModel.unverifiedTasks.observe(viewLifecycleOwner, Observer {
            // challenge
            Glide.with(binding.verifyChallengeImage.context).load(viewModel.unverifiedChallenge.value?.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
            ).into(binding.verifyChallengeImage)
            binding.verifyChallengeName.text = viewModel.unverifiedChallenge.value?.name
            binding.verifyChallengeStage.text = "共${viewModel.unverifiedChallenge.value?.stage}關"
            binding.verifyChallengeTime.text = "預計時長 ${viewModel.unverifiedChallenge.value?.timeSpent?.toDouble()?.toBigDecimal()?.div(3600.toDouble().toBigDecimal())} Hrs}"
            binding.verifyChallengeDescription.text = viewModel.unverifiedChallenge.value?.description
            // tasks
            taskAdapter.submitList(it)
        })
        viewModel.loadUnverifiedChallenge(challengeId)
        binding.agreeButton.setOnClickListener {
            viewModel.confirmChallenge(challengeId)
            this.findNavController().navigateUp()
        }
        binding.disagreeButton.setOnClickListener {
            viewModel.rejectChallenge(challengeId)
        }
        viewModel.navigateUp.observe(viewLifecycleOwner, Observer { deleteUnverified ->
            if (deleteUnverified) {
                this.findNavController().navigateUp()
                viewModel.navigateUpCompleted()
            }
        })


        return binding.root
    }
}