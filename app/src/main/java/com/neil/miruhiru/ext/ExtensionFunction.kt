package com.neil.miruhiru.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.MiruHiruApplication
import com.neil.miruhiru.R

fun ImageView.glideImage(uri: String) {
    Glide.with(this.context).load(uri).centerCrop().apply(
        RequestOptions().placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
    ).into(this)
}