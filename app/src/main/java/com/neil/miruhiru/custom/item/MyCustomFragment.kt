package com.neil.miruhiru.custom.item

import android.location.Geocoder
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
import com.neil.miruhiru.databinding.FragmentMyCustomBinding
import com.neil.miruhiru.network.LoadingStatus
import com.neil.miruhiru.task.TaskViewModel
import timber.log.Timber
import java.util.*

class MyCustomFragment : Fragment() {

    private val viewModel: MyCustomViewModel by lazy {
        ViewModelProvider(this).get(MyCustomViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMyCustomBinding.inflate(inflater, container, false)
        val customAdapter = MyCustomAdapter { customChallengeId ->
            this.findNavController().navigate(NavGraphDirections.actionGlobalOverviewFragment(customChallengeId))
        }
        binding.myCustomRecycler.adapter = customAdapter

        viewModel.myCustomList.observe(viewLifecycleOwner, Observer {
            customAdapter.submitList(it)
        })
        viewModel.loadMyCustom()

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




        return binding.root
    }

}