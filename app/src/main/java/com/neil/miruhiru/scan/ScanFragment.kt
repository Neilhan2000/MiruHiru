package com.neil.miruhiru.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.challengetype.ChallengeTypeViewModel
import com.neil.miruhiru.databinding.FragmentScanBinding
import kotlinx.coroutines.*

class ScanFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var codeScanner: CodeScanner
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val viewModel: ChallengeTypeViewModel by lazy {
        ViewModelProvider(this).get(ChallengeTypeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanBinding.inflate(inflater, container, false)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 123)
        } else {
            startScanning()
        }

        viewModel.navigateToTaskFragment.observe(viewLifecycleOwner, Observer { addToEvent ->
            if (addToEvent == "multiple") {
                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
            }
        })







        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun startScanning() {
        val scannerView = binding.scannerView
        codeScanner = CodeScanner(requireContext(), scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS

        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback { result ->
            scope.launch {
                Toast.makeText(requireContext(), "$result", Toast.LENGTH_SHORT).show()
                viewModel.addScanUserToEvent(result.text.toString(), "multiple")
            }
        }
        codeScanner.errorCallback = ErrorCallback { result ->
            scope.launch {
                Toast.makeText(requireContext(), "$result", Toast.LENGTH_SHORT).show()
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "camera permission granted", Toast.LENGTH_SHORT).show()
                startScanning()
            } else {
                Toast.makeText(requireContext(), "camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}