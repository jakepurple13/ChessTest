package com.crestron.aurora.utilities

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Interpolator
import android.widget.ImageView
import crestron.com.deckofcards.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.launch

class AnimationUtility {

    interface CountListener {
        fun numberUpdate(change: Number)
        fun endAnimation() {

        }
    }

    companion object Animation {

        fun translate(viewToMove: View, target: View) {
            viewToMove.animate()
                    .x(target.x)
                    .y(target.y)
                    .setDuration(1000)
                    .start()
        }

        fun circleAnimation(view: View) {
            val startRadius = 0
            val endRadius = Math.hypot(view.width.toDouble(), view.height.toDouble()).toInt()

            val anim = ViewAnimationUtils.createCircularReveal(view, view.width/2, view.height/2, startRadius.toFloat(), endRadius.toFloat())

            view.visibility = View.VISIBLE
            anim.start()
        }

        //private var animator: ValueAnimator? = null

        fun startCountAnimation(start: Int, end: Int, duration: Long = 500, interpolator: Interpolator? = null, view: View? = null, countListener: CountListener) {
            var animator: ValueAnimator? = null
            if (animator == null)
                animator = ValueAnimator.ofInt(start, end)
            else
                animator.setIntValues(animator.animatedValue as Int, end)
            animator!!.duration = duration
            animator.interpolator = interpolator
            animator.addUpdateListener { animation ->
                countListener.numberUpdate(animation.animatedValue as Number)
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    animation!!.removeAllListeners()
                    countListener.endAnimation()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })
            if (view != null)
                animator.setTarget(view)
            animator.start()
        }

        fun animateCard(cardView: ImageView, card: Card, context: Context, speed: Long = cardView.animate().duration, reverse: Boolean = false, end: AnimationEnd = object : AnimationEnd {}) = GlobalScope.launch(Dispatchers.Main) {
            cardView.rotationY = if (reverse) 360f else 0f
            cardView.animate().setDuration(speed).rotationY(if (reverse) 270f else 90f).setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    cardView.setImageResource(card.getImage(context))
                    cardView.rotationY = if (reverse) 90f else 270f
                    cardView.animate().rotationY(if (reverse) 0f else 360f).setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            end.onAnimationEnd()
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                    })
                }

                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

            })
        }

        fun animateCardWin(cardView: ImageView, card: Card, context: Context, speed: Long = cardView.animate().duration, reverse: Boolean = false, end: AnimationEnd = object : AnimationEnd {}) = GlobalScope.launch(Dispatchers.Main) {
            val zero = 0f
            val full = 360f
            val ninety = 90f
            val twoSeventy = 270f
            cardView.rotationY = if (reverse) full else zero
            cardView.rotationX = if (reverse) full else zero
            cardView.animate().setDuration(speed).rotationX(if (reverse) twoSeventy else ninety).rotationY(if (reverse) twoSeventy else ninety).setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    cardView.setImageResource(card.getImage(context))
                    cardView.rotationY = if (reverse) ninety else twoSeventy
                    cardView.rotationX = if (reverse) ninety else twoSeventy
                    cardView.animate().rotationX(if (reverse) zero else full).rotationY(if (reverse) zero else full).setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            end.onAnimationEnd()
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                    })
                }

                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

            })
        }

    }

    interface AnimationEnd {
        fun onAnimationEnd() {

        }
    }

}