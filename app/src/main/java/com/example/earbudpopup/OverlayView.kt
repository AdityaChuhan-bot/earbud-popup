package com.example.earbudpopup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.example.earbudpopup.databinding.OverlayBinding

class OverlayView(
    private val context: Context,
    private val deviceName: String,
    private val connected: Boolean
) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var binding: OverlayBinding? = null
    private var rootView: View? = null
    private var isShowing = false

    fun show() {
        if (isShowing) return

        val inflater = LayoutInflater.from(context)
        binding = OverlayBinding.inflate(inflater)
        rootView = binding?.root

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 80
        }

        binding?.apply {
            tvDeviceName.text = deviceName
            if (connected) {
                tvStatus.text = "Connected"
                tvStatus.setTextColor(context.getColor(R.color.status_connected))
                tvStatusIcon.text = "🎧"
                tvBattery.text = "Battery: Unknown"
                tvBattery.visibility = View.VISIBLE
            } else {
                tvStatus.text = "Disconnected"
                tvStatus.setTextColor(context.getColor(R.color.status_disconnected))
                tvStatusIcon.text = "🔇"
                tvBattery.visibility = View.GONE
            }
        }

        rootView?.alpha = 0f
        rootView?.translationY = -60f

        try {
            windowManager.addView(rootView, layoutParams)
            isShowing = true
            animateIn()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun animateIn() {
        val view = rootView ?: return

        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 280
            interpolator = DecelerateInterpolator()
        }
        val slideDown = ObjectAnimator.ofFloat(view, "translationY", -60f, 0f).apply {
            duration = 320
            interpolator = DecelerateInterpolator(1.8f)
        }

        AnimatorSet().apply {
            playTogether(fadeIn, slideDown)
            start()
        }
    }

    private fun animateOut(onComplete: () -> Unit) {
        val view = rootView ?: run {
            onComplete()
            return
        }

        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            duration = 250
            interpolator = AccelerateInterpolator()
        }
        val slideUp = ObjectAnimator.ofFloat(view, "translationY", 0f, -40f).apply {
            duration = 250
            interpolator = AccelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(fadeOut, slideUp)
            start()
        }

        view.postDelayed(onComplete, 270)
    }

    fun dismiss() {
        if (!isShowing) return
        isShowing = false

        animateOut {
            try {
                rootView?.let { windowManager.removeView(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                rootView = null
                binding = null
            }
        }
    }
}
