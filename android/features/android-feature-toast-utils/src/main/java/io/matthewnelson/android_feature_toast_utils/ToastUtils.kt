/*
*  Copyright 2021 Matthew Nelson
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* */
package io.matthewnelson.android_feature_toast_utils

import android.app.Service
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import kotlin.Exception

/**
 * Extend this class to apply your own default values.
 * */
open class ToastUtils(
    val toastLengthLong: Boolean = false,
    @DrawableRes val toastBackground: Int = R.drawable.toast_utils_default_background,
    @ColorRes val toastBackgroundTint: Int = R.color.toast_utils_default_background_tint,
    @ColorRes val textColor: Int = R.color.toast_utils_default_text,
    @DrawableRes val imageBackground: Int? = null,
    @DrawableRes val image: Int? = null
)

sealed class ToastUtilsResponse {
    class Success(val toast: Toast): ToastUtilsResponse()
    class Error(val e: Exception): ToastUtilsResponse()
}

fun ToastUtils.show(context: Context, @StringRes message: Int): ToastUtilsResponse {
    val string: String = try {
        context.applicationContext.resources.getString(message)
    } catch (e: Resources.NotFoundException) {
        return ToastUtilsResponse.Error(e)
    }
    return this.show(context, string)
}

@Suppress("DEPRECATION")
fun ToastUtils.show(context: Context, message: String): ToastUtilsResponse {
    val appContext = context.applicationContext
    (appContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as? LayoutInflater)?.let { inflater ->
        return try {
            val layout = inflater.inflate(
                appContext.resources.getLayout(R.layout.layout_toast_utils),
                null
            )

            layout.findViewById<LinearLayoutCompat>(R.id.layout_linear_toast)?.let { toastLayout ->
                toastLayout.background =
                    ContextCompat.getDrawable(appContext, toastBackground)

                    if (Build.VERSION.SDK_INT > 21) {
                        toastLayout.background.setTint(
                            Color.parseColor(
                                "#${Integer.toHexString(
                                    ContextCompat.getColor(appContext, toastBackgroundTint)
                                )}"
                            )
                        )
                    } else {
                        toastLayout.background.colorFilter = PorterDuffColorFilter(
                            appContext.resources.getColor(toastBackgroundTint),
                            PorterDuff.Mode.MULTIPLY
                        )
                    }

            } ?: return ToastUtilsResponse.Error(
                NullPointerException("findViewById layout_linear_toast returned null")
            )

            layout.findViewById<ImageView>(R.id.image_view_toast)?.let { imageView ->
                if (imageBackground == null && image == null) {
                    imageView.visibility = View.GONE
                } else {
                    imageBackground?.let { id ->
                        imageView.background = ContextCompat.getDrawable(appContext, id)
                    }
                    image?.let { id ->
                        imageView.setImageDrawable(
                            ContextCompat.getDrawable(appContext, id)
                        )
                    }
                }
            }
            layout.findViewById<TextView>(R.id.text_view_toast)?.let { textView ->
                textView.setTextColor(ContextCompat.getColor(appContext, textColor))
                textView.text = message
            } ?: return ToastUtilsResponse.Error(
                NullPointerException("findViewById text_view_toast returned null")
            )

            val toast = Toast(appContext)
            toast.view = layout
            toast.duration = if (toastLengthLong) {
                Toast.LENGTH_LONG
            } else {
                Toast.LENGTH_SHORT
            }

            toast.show()
            ToastUtilsResponse.Success(toast)
        } catch (e: Exception) {
            ToastUtilsResponse.Error(e)
        }
    } ?: return ToastUtilsResponse.Error(
        NullPointerException("LayoutInflater SystemService returned null")
    )
}