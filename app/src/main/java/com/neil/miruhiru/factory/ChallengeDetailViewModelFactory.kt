package com.neil.miruhiru.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neil.miruhiru.challengedetail.ChallengeDetailViewModel

class ChallengeDetailViewModelFactory constructor(private val challengeId: String) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        with(modelClass) {

            when {
                isAssignableFrom(ChallengeDetailViewModel::class.java) -> ChallengeDetailViewModel(challengeId)

                else ->
                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}"
                    )
            }
        } as T
}