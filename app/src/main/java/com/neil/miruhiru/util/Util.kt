package com.neil.miruhiru.util

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.MiruHiruApplication
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R

object Util {
    /**
     * Determine and monitor the connectivity status
     */
    fun isInternetConnected(): Boolean {
        val cm = MiruHiruApplication.instance
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    fun getString(resourceId: Int): String {
        return MiruHiruApplication.instance.getString(resourceId)
    }

    fun getColor(resourceId: Int): Int {
        return MiruHiruApplication.instance.getColor(resourceId)
    }

    fun showToast(message: String) {
        Toast.makeText(MiruHiruApplication.instance.applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun showDialog3Options(
        title: String, message: String,
        positiveFun: () -> Unit = {},
        negativeFun: () -> Unit = {},
        neutralFun: () -> Unit = {}
    ) {
        val defaultBuilder = AlertDialog.Builder(MainActivity.getInstanceFromMainActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                positiveFun()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                negativeFun()
            }
            .setNeutralButton(getString(R.string.clean_record)) { _, _ ->
                neutralFun()
            }
            .show()
        defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(getColor(R.color.deep_yellow))
        defaultBuilder.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(getColor(R.color.deep_yellow))
        defaultBuilder.getButton(DialogInterface.BUTTON_NEUTRAL)
            .setTextColor(getColor(R.color.deep_yellow))
    }

    fun showDialog2Options(
        title: String, message: String,
        positiveFun: () -> Unit = {},
        negativeFun: () -> Unit = {}
    ) {
        val defaultBuilder = AlertDialog.Builder(MainActivity.getInstanceFromMainActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                positiveFun()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                negativeFun()
            }
            .show()
        defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(getColor(R.color.deep_yellow))
        defaultBuilder.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(getColor(R.color.deep_yellow))
    }

    fun showDialog2OptionsNeu(
        title: String, message: String,
        positiveFun: () -> Unit = {},
        neutralFun: () -> Unit = {}
    ) {
        val defaultBuilder = AlertDialog.Builder(MainActivity.getInstanceFromMainActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                positiveFun()
            }
            .setNeutralButton(getString(R.string.clean_record)) { _, _ ->
                neutralFun()
            }
            .show()
        defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(getColor(R.color.deep_yellow))
        defaultBuilder.getButton(DialogInterface.BUTTON_NEUTRAL)
            .setTextColor(getColor(R.color.deep_yellow))
    }
}