package com.neil.miruhiru.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.MiruHiruApplication
import com.neil.miruhiru.R

fun ImageView.glideImageCenter(uri: String?, placeHolder: Int) {
    Glide.with(this.context).load(uri).centerCrop().apply(
        RequestOptions().placeholder(placeHolder).error(placeHolder)
    ).into(this)
}

fun ImageView.glideImageCircle(uri: String?, placeHolder: Int) {
    Glide.with(this.context).load(uri).circleCrop().apply(
        RequestOptions().placeholder(placeHolder).error(placeHolder)
    ).into(this)
}