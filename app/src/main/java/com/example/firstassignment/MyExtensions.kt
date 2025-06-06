package com.example.firstassignment

import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.load(resourceId: Int) {
    Glide.with(this.context)
        .load(resourceId)
        .into(this)
}
