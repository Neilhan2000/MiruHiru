package com.neil.miruhiru.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentProfileBinding
import timber.log.Timber


class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.button2.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalScanFragment())
        }

        val db = Firebase.firestore
        var eventDocumentId = ""

        // get event document id
//        db.collection("events").whereEqualTo("id", "cfb5d07a-9457-4761-968d-4598f6879c26")
//            .get()
//            .addOnSuccessListener { result ->
//                eventDocumentId = result.documents[0].id
//                Timber.i("id $eventDocumentId")
//            }

        return binding.root
    }

}