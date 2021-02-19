package io.matthewnelson.android_feature_views.ui.base

import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<
        VM: ViewModel,
        VB: ViewBinding
        >(@LayoutRes layoutId: Int): AppCompatActivity(layoutId)
{
    protected abstract val viewModel: VM
    protected abstract val binding: VB
}
