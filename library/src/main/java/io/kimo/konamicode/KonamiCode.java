package io.kimo.konamicode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class KonamiCode {

    private Context mContext;
    private ViewGroup mRootView;
    private KonamiCodeLayout.Callback mCallback;
    private KonamiCodeLayout konamiCodeLayout;

    public KonamiCodeLayout getKonamiCodeLayout() {
        return konamiCodeLayout;
    }
    public KonamiCodeLayout.Callback getCallback() {
        return mCallback;
    }
    public Context getContext() {
        return mContext;
    }
    public ViewGroup getRootView() {
        return mRootView;
    }

    private KonamiCode(@NonNull Installer installer) {
        this.mContext = installer.context;
        this.mRootView = installer.rootView;
        this.mCallback = installer.callback;
        this.konamiCodeLayout = installer.layout;
    }

    public static class Installer {
        protected Context context;
        protected ViewGroup rootView;
        protected KonamiCodeLayout.Callback callback;
        protected KonamiCodeLayout layout;

        /**
         * Konami Code's builder
         * @param context
         */
        public Installer(@NonNull Context context) {
            this.context = context;
        }

        /**
         * on - installs into an activity
         * @param activity
         */
        public Installer on(@NonNull Activity activity) {
            rootView = (ViewGroup) activity.findViewById(android.R.id.content);
            return this;
        }

        /**
         * into - installs into a fragment
         * @param fragment
         */
        public Installer on(@NonNull Fragment fragment) {
            rootView = (ViewGroup) fragment.getView().getRootView();
            return this;
        }

        /**
         * into - installs into a view
         * @param view
         */
        public Installer on(@NonNull View view) {
            rootView = (ViewGroup) view.getRootView();
            return this;
        }

        /**
         * withCallback - interface executed after the whole code is executed
         * @param callback
         */
        public Installer callback(@NonNull KonamiCodeLayout.Callback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * install - installs all Konami Code components into the target
         */
        @SuppressLint("ClickableViewAccessibility")
        public KonamiCode install() {

            View currentView = rootView.getChildAt(0);
            rootView.removeView(currentView);

            //match parent params
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            FrameLayout gestureDelegate = new FrameLayout(context);
            gestureDelegate.addView(currentView, layoutParams);

            //necessary view that passes all touch events up to the parent viewgroup
            gestureDelegate.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            layout = new KonamiCodeLayout(context);
            layout.addView(gestureDelegate);

            rootView.addView(layout, layoutParams);

            layout.setCallback(callback);

            return new KonamiCode(this);
        }

        @SuppressLint("ClickableViewAccessibility")
        public KonamiCode install(KonamiCodeLayout.Direction sequence[], KonamiCodeLayout.Button buttons[]) {

            View currentView = rootView.getChildAt(0);
            rootView.removeView(currentView);

            //match parent params
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            FrameLayout gestureDelegate = new FrameLayout(context);
            gestureDelegate.addView(currentView, layoutParams);

            //necessary view that passes all touch events up to the parent viewgroup
            gestureDelegate.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            layout = new KonamiCodeLayout(context);
            layout.addView(gestureDelegate);

            rootView.addView(layout, layoutParams);

            layout.setCallback(callback);
            layout.setSequence(sequence);
            layout.setButtonOrder(buttons);

            return new KonamiCode(this);
        }

        @SuppressLint("ClickableViewAccessibility")
        public KonamiCode install(KonamiCodeLayout.Direction sequence[]) {

            View currentView = rootView.getChildAt(0);
            rootView.removeView(currentView);

            //match parent params
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            FrameLayout gestureDelegate = new FrameLayout(context);
            gestureDelegate.addView(currentView, layoutParams);

            //necessary view that passes all touch events up to the parent viewgroup
            gestureDelegate.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            KonamiCodeLayout konamiCodeLayout = new KonamiCodeLayout(context);
            konamiCodeLayout.addView(gestureDelegate);

            rootView.addView(konamiCodeLayout, layoutParams);

            konamiCodeLayout.setCallback(callback);
            konamiCodeLayout.setSequence(sequence);

            return new KonamiCode(this);
        }

        @SuppressLint("ClickableViewAccessibility")
        public KonamiCode install(KonamiCodeLayout.Button buttons[]) {

            View currentView = rootView.getChildAt(0);
            rootView.removeView(currentView);

            //match parent params
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            FrameLayout gestureDelegate = new FrameLayout(context);
            gestureDelegate.addView(currentView, layoutParams);

            //necessary view that passes all touch events up to the parent viewgroup
            gestureDelegate.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            KonamiCodeLayout konamiCodeLayout = new KonamiCodeLayout(context);
            konamiCodeLayout.addView(gestureDelegate);

            rootView.addView(konamiCodeLayout, layoutParams);

            konamiCodeLayout.setCallback(callback);
            konamiCodeLayout.setButtonOrder(buttons);

            return new KonamiCode(this);
        }

    }
}
