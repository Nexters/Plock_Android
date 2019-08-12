package com.teamnexters.plock.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

fun Context.start(kClass: KClass<out Activity>) {
    val intent = Intent(this, kClass.java)
    startActivity(intent)
}

fun Fragment.start(kClass: KClass<out Activity>) {
    val intent = Intent(activity, kClass.java)
    startActivity(intent)
}
