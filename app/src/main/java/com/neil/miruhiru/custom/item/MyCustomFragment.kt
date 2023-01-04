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
import kotlinx.coroutines.*
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
        val customAdapter = MyCustomAdapter(viewModel) { customChallengeId ->
            this.findNavController().navigate(NavGraphDirections.actionGlobalOverviewFragment(customChallengeId))
        }
        binding.myCustomRecycler.adapter = customAdapter

        viewModel.myCustomList.observe(viewLifecycleOwner, Observer {
            customAdapter.submitList(it)
            customAdapter.notifyDataSetChanged()
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

        // show or hide delete texts
        viewModel.showDeleteText.observe(viewLifecycleOwner, Observer { show ->
            if (show) {
                // change toolbar title
                (activity as MainActivity).binding.toolbarTitle.text = getString(R.string.select_to_delete)
                // cancel
                (activity as MainActivity).binding.cancelText.visibility = View.VISIBLE
                (activity as MainActivity).binding.cancelText.setOnClickListener {
                    viewModel.cancelLongClick()
                    viewModel.cleanSelectedPositions()
                    customAdapter.notifyDataSetChanged()
                    viewModel.hideDeleteText()
                }
                // delete
                (activity as MainActivity).binding.deleteText.visibility = View.VISIBLE
                (activity as MainActivity).binding.deleteText.setOnClickListener {
                    viewModel.deleteSelectedItems()
                    viewModel.hideDeleteText()
                }
            }
            else {
                (activity as MainActivity).binding.toolbarTitle.text = getString(R.string.custom_fragment)
                (activity as MainActivity).binding.cancelText.visibility = View.GONE
                (activity as MainActivity).binding.deleteText.visibility = View.GONE
            }
        })




        return binding.root
    }

}