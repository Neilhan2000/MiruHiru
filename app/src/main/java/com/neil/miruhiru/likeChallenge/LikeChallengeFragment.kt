package com.neil.miruhiru.likeChallenge

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentLikeChallengeBinding
import com.neil.miruhiru.invite.InviteFragmentViewModel
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber

class LikeChallengeFragment : Fragment() {

    private lateinit var binding: FragmentLikeChallengeBinding
    private val viewModel: LikeChallengeViewModel by lazy {
        ViewModelProvider(this).get(LikeChallengeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLikeChallengeBinding.inflate(inflater, container, false)

        val challengeAdapter = LikeChallengeAdapter { challengeId ->
            this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeDetailFragment(challengeId))
        }
        binding.likeChallengeRecycler.adapter = challengeAdapter
        viewModel.likeChallengeList.observe(viewLifecycleOwner, Observer {
            challengeAdapter.submitList(it)
            if (it.size == 0) {
                binding.hint.visibility = View.VISIBLE
                binding.lottieAnimationView.visibility = View.VISIBLE
            }
        })

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
        viewModel.loadLikeChallenges()

        return binding.root
    }

}