package com.neil.miruhiru.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentLogBinding
import com.neil.miruhiru.databinding.FragmentLoginBinding
import com.neil.miruhiru.log.LogViewModel
import timber.log.Timber


class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by lazy {
        ViewModelProvider(this).get(SignInViewModel::class.java)
    }

    companion object {
        private const val GOOGLE_SIGN_IN_CODE = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        val googleTextView: TextView = binding.signInButton.getChildAt(0) as TextView
        googleTextView.text = getString(R.string.sign_in_with_google)

        binding.signInButton.setOnClickListener {
            viewModel.signIn(this)
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            viewModel.handleSignInResult(task)
        }
    }


}