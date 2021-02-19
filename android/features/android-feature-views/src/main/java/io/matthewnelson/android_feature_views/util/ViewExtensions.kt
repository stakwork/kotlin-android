package io.matthewnelson.android_feature_views.util

import android.content.res.Resources
import android.view.View

@Suppress("NOTHING_TO_INLINE")
inline fun Int.dpToPX(): Int =
    (this * Resources.getSystem().displayMetrics.density).toInt()

@Suppress("NOTHING_TO_INLINE")
inline fun Int.pxToDp(): Int =
    (this / Resources.getSystem().displayMetrics.density).toInt()

@Suppress("NOTHING_TO_INLINE")
inline fun View.invisibleIfFalse(boolean: Boolean) {
    this.visibility = if (boolean) View.VISIBLE else View.INVISIBLE
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.goneIfFalse(boolean: Boolean) {
    this.visibility = if (boolean) View.VISIBLE else View.GONE
}
