package hb.xvideoplayer;

import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


/**
 * Manager all VideoPlayerListener
 * put MxVideoPlayer into Layout
 */
public class MxVideoPlayerManager {
    public static WeakReference<MxMediaPlayerListener> mCurScrollListener;
    public static LinkedList<WeakReference<MxMediaPlayerListener>> mListenerList = new LinkedList<>();

    public static void putScrollListener(MxMediaPlayerListener listener) {
        if (listener.getScreenType() == MxVideoPlayer.SCREEN_WINDOW_TINY ||
                listener.getScreenType() == MxVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
            return;
        }
        mCurScrollListener = new WeakReference<>(listener);
    }

    public static void putListener(MxMediaPlayerListener listener) {
        if (listener == null) {
            return;
        }
        mListenerList.push(new WeakReference<>(listener));
    }

    public static void checkAndPutListener(MxMediaPlayerListener listener) {
        if (listener == null || mListenerList == null) {
            return;
        }
        if (listener.getScreenType() == MxVideoPlayer.SCREEN_WINDOW_TINY ||
                listener.getScreenType() == MxVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
            return;
        }
        int location = -1;
        for (int i = 1; i < mListenerList.size(); ++i) {
            MxMediaPlayerListener mediaPlayerListener = mListenerList.get(i).get();
            String url = listener.getUrl();
            String url1 = mediaPlayerListener.getUrl();
            if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(url1) && url.equals(url1)) {
                location = i;
            }
        }

        if (location != -1) {
            mListenerList.remove(location);
            if (mListenerList.size() <= location) {
                mListenerList.addLast(new WeakReference<>(listener));
            } else {
                mListenerList.set(location, new WeakReference<>(listener));
            }
        }
    }

    public static MxMediaPlayerListener popListener() {
        if (mListenerList.size() <= 0) {
            return null;
        }
        return mListenerList.pop().get();
    }

    public static MxMediaPlayerListener getCurrentListener() {
        if (mListenerList.size() <= 0) {
            return null;
        }
        return mListenerList.getFirst().get();
    }

    public static void completeAll() {
        MxMediaPlayerListener listener = popListener();
        while (listener != null) {
            listener.onCompletion();
            listener = popListener();
        }
    }
}
