package hb.xvideoplayer;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import mxvideoplayer.app.com.xvideoplayer.R;

public class MxTvPlayerWidget extends MxVideoPlayer {

    private static final int VOLUME_ITEM = 2;
    private static final int PROGRESS_ITEM = 15;

    protected static Timer DISMISS_CONTROL_VIEW_TIMER;

    public ProgressBar mBottomProgressBar, mLoadingProgressBar;
    public TextView mTitleTextView;
    public ImageView mThumbImageView;

    protected Dialog mProgressDialog;
    protected Dialog mVolumeDialog;
    protected ProgressBar mDialogVolumeProgressBar;
    protected ProgressBar mDialogProgressBar;

    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected ImageView mDialogIcon;
    protected ImageView mDialogVolumeIcon;

    private int mTvDownPosition = 0;
    private int mTvSeekPosition;

    private OnPlayStateListener mListener;
    private IOnKeyListener mKeyListener;
    private boolean mIsShowBottomProgressBar;

    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    private Runnable mDismissVolumeCallback = new Runnable() {
        @Override
        public void run() {
            dismissVolumeDialog();
        }
    };

    public MxTvPlayerWidget(Context context) {
        super(context);
    }

    public MxTvPlayerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initView(Context context) {
        super.initView(context);
        mBottomProgressBar = (ProgressBar) findViewById(R.id.mx_bottom_progress);
        mTitleTextView = (TextView) findViewById(R.id.mx_title);
        mThumbImageView = (ImageView) findViewById(R.id.mx_thumb);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.mx_loading);
    }

    public boolean startPlay(String url, Object... objects) {
        if (objects.length == 0) {
            return false;
        }
        if (super.startPlay(url, SCREEN_LAYOUT_NORMAL, objects)) {
            mTitleTextView.setText(objects[0].toString());
            return true;
        }
        return false;
    }

    public boolean autoStartPlay(String url, Object... objects) {
        if (startPlay(url, objects)) {
            mPlayControllerButton.performClick();
            return true;
        } else {
            return false;
        }
    }

    public void setOnPlayStateListener(OnPlayStateListener listener) {
        mListener = listener;
    }

    public void addOnKeyListener(IOnKeyListener listener) {
        mKeyListener = listener;
    }

    public boolean requestKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                downVolume();
                result = true;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_VOLUME_UP:
                upVolume();
                result = true;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                cancelProgressTimer();
                if (mTvDownPosition == 0) {
                    mTvDownPosition = getCurrentPositionWhenPlaying();
                }
                doReverse(event.getRepeatCount());
                result = true;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                cancelProgressTimer();
                if (mTvDownPosition == 0) {
                    mTvDownPosition = getCurrentPositionWhenPlaying();
                }
                doForward(event.getRepeatCount());
                result = true;
                break;
            case KeyEvent.KEYCODE_BACK:
                result = true;
                MxUtils.getAppComptActivity(getContext()).onBackPressed();
                break;
            default:
                break;
        }
        if (!result && mKeyListener != null) {
            return mKeyListener.onKeyDown(keyCode, event);
        }
        return result;
    }

    public boolean requestKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                mPlayControllerButton.performClick();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                startProgressTimer();
                setProgress();
                dismissProgressDialog();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mHandler.removeCallbacks(mDismissVolumeCallback);
                mHandler.postDelayed(mDismissVolumeCallback, 2000);
                break;
            default:
                onClickUiToggle();
                startDismissControlViewTimer();
                break;
        }

        return mKeyListener != null && mKeyListener.onKeyUp(keyCode, event);
    }

    public void downVolume() {
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - VOLUME_ITEM, 0);
        int volumePercent = (currentVolume - VOLUME_ITEM) * 100 / maxVolume;
        showVolumeDialog(VOLUME_ITEM, volumePercent);
    }

    public void upVolume() {
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + VOLUME_ITEM, 0);
        int volumePercent = (currentVolume + VOLUME_ITEM) * 100 / maxVolume;
        showVolumeDialog(VOLUME_ITEM, volumePercent);
    }

    public void doReverse(int count) {
        if (count <= 0) {
            count = 1;
        }
        int totalTimeDuration = getDuration();
        int seekTimePosition = mTvDownPosition + PROGRESS_ITEM * count * (totalTimeDuration / mScreenWidth);
        if (seekTimePosition > totalTimeDuration) {
            seekTimePosition = totalTimeDuration;
        }
        String seekTime = MxUtils.stringForTime(seekTimePosition);
        String totalTime = MxUtils.stringForTime(totalTimeDuration);
        showProgressDialog(PROGRESS_ITEM, seekTime, seekTimePosition, totalTime, totalTimeDuration);
    }

    public void doForward(int count) {
        if (count <= 0) {
            count = 1;
        }
        int totalTimeDuration = getDuration();
        int seekTimePosition = mTvDownPosition - PROGRESS_ITEM * count * (totalTimeDuration / mScreenWidth);
        if (seekTimePosition < 0) {
            seekTimePosition = 0;
        }
        String seekTime = MxUtils.stringForTime(seekTimePosition);
        String totalTime = MxUtils.stringForTime(totalTimeDuration);
        showProgressDialog(-PROGRESS_ITEM, seekTime, seekTimePosition, totalTime, totalTimeDuration);
    }

    private void setProgress() {
        MxMediaManager.getInstance().getPlayer().seekTo(mTvSeekPosition);
        mProgressBar.setProgress(mDialogProgressBar.getProgress());
        mTvDownPosition = 0;
    }

    @Override
    public void setUiPlayState(int state) {
        super.setUiPlayState(state);
        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL:
                setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case CURRENT_STATE_PREPARING:
                setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE);
                startDismissControlViewTimer();
                mBottomProgressBar.setProgress(0);
                break;
            case CURRENT_STATE_PLAYING:
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                startDismissControlViewTimer();
                break;
            case CURRENT_STATE_PAUSE:
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                cancelDismissControlViewTimer();
                break;
            case CURRENT_STATE_ERROR:
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage();
                cancelDismissControlViewTimer();
                mBottomProgressBar.setProgress(100);
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    private void onClickUiToggle() {
        if (mCurrentState == CURRENT_STATE_PREPARING) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE);
            } else {
                setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE);
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
            } else {
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
            }
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mBottomProgressBar.getVisibility() == View.VISIBLE) {
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
            } else {
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
            }
        } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.VISIBLE);
                updateStartImage();
            } else {
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage();
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.VISIBLE);
                updateStartImage();
            } else {
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
            }
        }
    }

    private void cancelDismissControlViewTimer() {
        if (DISMISS_CONTROL_VIEW_TIMER != null) {
            DISMISS_CONTROL_VIEW_TIMER.cancel();
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
        }
    }

    private void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        DISMISS_CONTROL_VIEW_TIMER = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        DISMISS_CONTROL_VIEW_TIMER.schedule(mDismissControlViewTimerTask, 2500);
    }

    private void updateStartImage() {
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            mPlayControllerButton.setImageResource(R.drawable.mx_click_pause_selector);
        } else if (mCurrentState == CURRENT_STATE_ERROR) {
            mPlayControllerButton.setImageResource(R.drawable.mx_click_error_selector);
        } else {
            mPlayControllerButton.setImageResource(R.drawable.mx_click_play_selector);
        }
    }

    public void setAllControlsVisible(int topCon, int bottomCon, int startBtn, int loadingPro,
                                      int thumbImg, int bottomPro) {
        mTopContainer.setVisibility(topCon);
        mBottomContainer.setVisibility(bottomCon);
        mPlayControllerButton.setVisibility(startBtn);
        mLoadingProgressBar.setVisibility(loadingPro);
        if (thumbImg == View.VISIBLE) {
            mThumbImageView.setVisibility(thumbImg);
        } else {
            mThumbImageView.setVisibility(View.GONE);
        }
        if (mIsShowBottomProgressBar) {
            mBottomProgressBar.setVisibility(bottomPro);
        } else {
            mBottomProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime);
        if (progress != 0) {
            mBottomProgressBar.setProgress(progress);
        }
        if (secProgress != 0) {
            mBottomProgressBar.setSecondaryProgress(secProgress);
        }
    }

    @Override
    protected void showBrightnessDialog(float v, int brightnessPercent) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_video_layout_tv;
    }

    @Override
    protected void initAttributeSet(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.MxVideoPlayer);
        Drawable drawable = attr.getDrawable(R.styleable.MxVideoPlayer_progress_drawable);
        setProgressDrawable(drawable);
        int defaultTextSize = context.getResources().getDimensionPixelSize(R.dimen.mx_title_textSize);
        int size = attr.getDimensionPixelSize(R.styleable.MxVideoPlayer_title_size, defaultTextSize);
        setTitleSize(size);
        boolean isShowBottomProgressBar = attr.getBoolean(R.styleable.MxVideoPlayer_showBottomProgress, true);
        setBottomProgressBarVisibility(isShowBottomProgressBar);
        attr.recycle();
    }

    private void setProgressDrawable(Drawable drawable) {
        if (drawable != null) {
            mProgressBar.setProgressDrawable(drawable);
        }
    }

    private void setTitleSize(int size) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void setBottomProgressBarVisibility(boolean visibility) {
        mIsShowBottomProgressBar = visibility;
    }

    @Override
    protected boolean isShowNetworkStateDialog() {
        if (!mPlayUrl.startsWith("file") && !MxUtils.isNetworkConnected(getContext())) {
            showNetworkTipDialog();
            return true;
        }
        return false;
    }

    private void showNetworkTipDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_net));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_network_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void showProgressDialog(float deltaX, String seekTime,
                                      int seekTimePosition, String totalTime, int totalTimeDuration) {
        if (mProgressDialog == null) {
            View localView = View.inflate(getContext(), R.layout.mx_tv_progress_dialog, null);
            mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
            mDialogSeekTime = ((TextView) localView.findViewById(R.id.tv_current));
            mDialogTotalTime = ((TextView) localView.findViewById(R.id.tv_duration));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mProgressDialog = new Dialog(getContext(), R.style.mx_style_dialog_progress);
            mProgressDialog.setContentView(localView);
            if (mProgressDialog.getWindow() != null) {
                mProgressDialog.getWindow().addFlags(Window.FEATURE_ACTION_BAR);
                mProgressDialog.getWindow().addFlags(32);
                mProgressDialog.getWindow().addFlags(16);
                mProgressDialog.getWindow().setLayout(-2, -2);
            }
            WindowManager.LayoutParams localLayoutParams = mProgressDialog.getWindow().getAttributes();
            localLayoutParams.gravity = 49;
            localLayoutParams.y = getResources().getDimensionPixelOffset(R.dimen.mx_tv_progress_dialog_margin_top);
            localLayoutParams.width = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.tv_progress_dialog_width);
            mProgressDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(String.format(" / %s", totalTime));
        mTvSeekPosition = seekTimePosition;
        int curProgress = seekTimePosition * 100 / totalTimeDuration;
        mDialogProgressBar.setProgress(totalTimeDuration <= 0 ? 0 : curProgress);
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.mx_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.mx_backward_icon);
        }
    }

    @Override
    protected void showVolumeDialog(float v, int volumePercent) {
        if (mVolumeDialog == null) {
            View localView = View.inflate(getContext(), R.layout.mx_tv_volume_dialog, null);
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            mDialogVolumeIcon = (ImageView) localView.findViewById(R.id.mx_volume_icon);
            mVolumeDialog = new Dialog(getContext(), R.style.mx_style_dialog_progress);
            mVolumeDialog.setContentView(localView);
            if (mVolumeDialog.getWindow() != null) {
                mVolumeDialog.getWindow().addFlags(8);
                mVolumeDialog.getWindow().addFlags(32);
                mVolumeDialog.getWindow().addFlags(16);
                mVolumeDialog.getWindow().setLayout(-2, -2);
            }
            WindowManager.LayoutParams localLayoutParams = mVolumeDialog.getWindow().getAttributes();
            localLayoutParams.gravity = 49;
            localLayoutParams.y = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.mx_tv_progress_dialog_margin_top);
            localLayoutParams.width = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.tv_volume_dialog_ll_width);
            mVolumeDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }
        if (volumePercent <= 0) {
            mDialogVolumeIcon.setImageResource(R.drawable.mx_volume_no);
        } else {
            mDialogVolumeIcon.setImageResource(R.drawable.mx_volume_icon);
        }
        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    @Override
    protected void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
    }

    @Override
    protected void dismissBrightnessDialog() {

    }

    @Override
    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        if (mListener != null) {
            mListener.onPlayPrepared();
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        super.onBufferingUpdate(percent);
        if (mListener != null) {
            mListener.onPlayBufferingUpdate(percent);
        }
    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        if (mListener != null) {
            mListener.OnPlayCompletion();
        }
    }

    private class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mCurrentState != CURRENT_STATE_NORMAL
                    && mCurrentState != CURRENT_STATE_ERROR
                    && mCurrentState != CURRENT_STATE_AUTO_COMPLETE) {
                if (getContext() != null && getContext() instanceof Activity) {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBottomContainer.setVisibility(View.INVISIBLE);
                            mTopContainer.setVisibility(View.INVISIBLE);
                            mPlayControllerButton.setVisibility(View.INVISIBLE);
                            if (mIsShowBottomProgressBar) {
                                mBottomProgressBar.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        }
    }

    public interface OnPlayStateListener {
        void onPlayPrepared();

        void onPlayBufferingUpdate(int percent);

        void OnPlayCompletion();
    }

    public interface IOnKeyListener {
        boolean onKeyDown(int keyCode, KeyEvent event);

        boolean onKeyUp(int keyCode, KeyEvent event);
    }
}
