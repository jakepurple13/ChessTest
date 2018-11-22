package hb.xvideoplayer;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.Timer;
import java.util.TimerTask;

import mxvideoplayer.app.com.xvideoplayer.R;

public class MxVideoPlayerWidget extends MxVideoPlayer {

    protected static Timer DISMISS_CONTROL_VIEW_TIMER;

    public ImageView mBackButton;
    public ProgressBar mBottomProgressBar, mLoadingProgressBar;
    public TextView mTitleTextView;
    public ImageView mThumbImageView;
    public ImageView mTinyBackImageView;

    protected Dialog mLockDialog;
    protected Dialog mProgressDialog;
    protected Dialog mVolumeDialog;
    protected Dialog mBrightnessDialog;
    protected ImageButton mDialogLockView;
    protected ProgressBar mDialogVolumeProgressBar;
    protected ProgressBar mDialogBrightnessProgressBar;
    protected ProgressBar mDialogProgressBar;
    //protected PreviewSeekBar mDialogProgressBar;
    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected ImageView mDialogIcon;
    protected boolean mIsShowBottomProgressBar;
    private boolean mIsAutoPlay = false;
    private boolean mIsAutoProcessUI = false;
    private UIStatusChangeListener mUIListener;
    public boolean hideFullScreenButton = false;

    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    public enum Mode {
        MODE_NORMAL,
        MODE_PREPARING,
        MODE_PREPARING_CLEAR,
        MODE_AUTO_PLAY,
        MODE_PLAYING,
        MODE_PLAYING_CLEAR,
        MODE_PAUSE,
        MODE_PAUSE_CLEAR,
        MODE_COMPLETE,
        MODE_COMPLETE_CLEAR,
        MODE_BUFFERING,
        MODE_BUFFERING_CLEAR,
        MODE_ERROR
    }

    public MxVideoPlayerWidget(Context context) {
        super(context);
    }

    public MxVideoPlayerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initView(Context context) {
        super.initView(context);
        mBottomProgressBar = (ProgressBar) findViewById(R.id.mx_bottom_progress);
        mTitleTextView = (TextView) findViewById(R.id.mx_title);
        mBackButton = (ImageView) findViewById(R.id.mx_back);
        mThumbImageView = (ImageView) findViewById(R.id.mx_thumb);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.mx_loading);
        mTinyBackImageView = (ImageView) findViewById(R.id.mx_quit_tiny);

        mThumbImageView.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mTinyBackImageView.setOnClickListener(this);
    }

    public boolean autoStartPlay(String url, int screen, Object... objects) {
        mIsAutoPlay = true;
        boolean result = false;
        if (startPlay(url, screen, objects)) {
            if (TextUtils.isEmpty(mPlayUrl)) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            if (mCurrentState == CURRENT_STATE_NORMAL) {
                if (isShowNetworkStateDialog()) {
                    changeUiShowState(Mode.MODE_NORMAL);
                    return false;
                }
            }
            preparePlayVideo();
            result = true;
        }
        mIsAutoPlay = false;
        return result;
    }

    @Override
    public boolean startPlay(String url, int screen, Object... objects) {
        if (objects.length == 0) {
            return false;
        }
        if (super.startPlay(url, screen, objects)) {
            mTitleTextView.setText(objects[0].toString());
            if (mCurrentScreen == SCREEN_WINDOW_FULLSCREEN) {
                mFullscreenButton.setImageResource(R.drawable.mx_shrink);
                if (hideFullScreenButton)
                    mFullscreenButton.setVisibility(View.GONE);
                mBackButton.setVisibility(View.VISIBLE);
                mTinyBackImageView.setVisibility(View.INVISIBLE);
            } else if (mCurrentScreen == SCREEN_LAYOUT_LIST ||
                    mCurrentScreen == SCREEN_LAYOUT_NORMAL) {
                mFullscreenButton.setImageResource(R.drawable.mx_enlarge);
                mBackButton.setVisibility(View.GONE);
                mTinyBackImageView.setVisibility(View.INVISIBLE);
            } else if (mCurrentScreen == SCREEN_WINDOW_TINY) {
                mTinyBackImageView.setVisibility(View.VISIBLE);
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
            }
            return true;
        }
        return false;
    }

    public void setUIStatusListener(UIStatusChangeListener listener) {
        mUIListener = listener;
    }

    public void setAutoProcessUI(boolean autoPrcUI) {
        mIsAutoProcessUI = autoPrcUI;
    }

    @Override
    public void setUiPlayState(int state) {
        super.setUiPlayState(state);
        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL:
                if (!mIsAutoPlay) {
                    changeUiShowState(Mode.MODE_NORMAL);
                } else {
                    changeUiShowState(Mode.MODE_AUTO_PLAY);
                }
                break;
            case CURRENT_STATE_PREPARING:
                changeUiShowState(Mode.MODE_PREPARING);
                startDismissControlViewTimer();
                mBottomProgressBar.setProgress(0);
                break;
            case CURRENT_STATE_PLAYING:
                changeUiShowState(Mode.MODE_PLAYING);
                startDismissControlViewTimer();
                break;
            case CURRENT_STATE_PAUSE:
                changeUiShowState(Mode.MODE_PAUSE);
                cancelDismissControlViewTimer();
                break;
            case CURRENT_STATE_ERROR:
                changeUiShowState(Mode.MODE_ERROR);
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                changeUiShowState(Mode.MODE_COMPLETE);
                cancelDismissControlViewTimer();
                mBottomProgressBar.setProgress(100);
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                changeUiShowState(Mode.MODE_BUFFERING);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        showLockDialog();
        if (!mLocked) {
            int id = v.getId();
            if (id == R.id.mx_surface_container) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        startDismissControlViewTimer();
                        if (mChangePosition) {
                            int duration = getDuration();
                            int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
                            mBottomProgressBar.setProgress(progress);
                        }
                        if (!mChangePosition && !mChangeVolume) {
                            onClickUiToggle();
                        }
                        break;
                    default:
                        break;
                }
            } else if (id == R.id.mx_progress) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cancelDismissControlViewTimer();
                        break;
                    case MotionEvent.ACTION_UP:
                        startDismissControlViewTimer();
                        break;
                    default:
                        break;
                }
            }
        }
        return super.onTouch(v, event);
    }

    private void onClickUiToggle() {
        if (mCurrentState == CURRENT_STATE_PREPARING) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiShowState(Mode.MODE_PREPARING_CLEAR);
            } else {
                changeUiShowState(Mode.MODE_PREPARING);
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiShowState(Mode.MODE_PLAYING_CLEAR);
            } else {
                changeUiShowState(Mode.MODE_PLAYING);
            }
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mBottomProgressBar.getVisibility() == View.VISIBLE) {
                changeUiShowState(Mode.MODE_PAUSE_CLEAR);
            } else {
                changeUiShowState(Mode.MODE_PAUSE);
            }
        } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiShowState(Mode.MODE_COMPLETE_CLEAR);
            } else {
                changeUiShowState(Mode.MODE_COMPLETE);
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiShowState(Mode.MODE_BUFFERING_CLEAR);
            } else {
                changeUiShowState(Mode.MODE_BUFFERING);
            }
        }
    }

    private void changeUiShowState(Mode mode) {
        if (mCurrentScreen == SCREEN_WINDOW_TINY) {
            return;
        }

        if (mUIListener != null) {
            mUIListener.onUIChange(mode);
        }

        if (mIsAutoProcessUI) {
            if (mode == Mode.MODE_NORMAL || mode == Mode.MODE_BUFFERING_CLEAR ||
                    mode == Mode.MODE_PLAYING || mode == Mode.MODE_PAUSE ||
                    mode == Mode.MODE_COMPLETE || mode == Mode.MODE_COMPLETE_CLEAR
                    || mode == Mode.MODE_ERROR) {
                updateStartImage();
            }
            return;
        }

        switch (mode) {
            case MODE_NORMAL:
                setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case MODE_BUFFERING:
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case MODE_BUFFERING_CLEAR:
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.VISIBLE);
                updateStartImage();
                break;
            case MODE_AUTO_PLAY:
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case MODE_PREPARING:
                setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case MODE_PREPARING_CLEAR:
                setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case MODE_PLAYING:
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case MODE_PLAYING_CLEAR:
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                break;
            case MODE_PAUSE:
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case MODE_PAUSE_CLEAR:
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case MODE_COMPLETE:
                setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case MODE_COMPLETE_CLEAR:
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.VISIBLE);
                updateStartImage();
                break;
            case MODE_ERROR:
                setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
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
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.mx_thumb) {
            if (TextUtils.isEmpty(mPlayUrl)) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentState == CURRENT_STATE_NORMAL) {
                if (!mPlayUrl.startsWith("file") && !MxUtils.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
                    showWifiDialog();
                    return;
                }
                preparePlayVideo();
            } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
                onClickUiToggle();
            }
        } else if (id == R.id.mx_surface_container) {
            startDismissControlViewTimer();
        } else if (id == R.id.mx_back) {
            backPress();
        } else if (id == R.id.mx_quit_tiny) {
            if (MxVideoPlayerManager.mCurScrollListener.get() != null) {
                if (!MxVideoPlayerManager.mCurScrollListener.get().getUrl().
                        equals(MxMediaManager.getInstance().getPlayer().getDataSource())) {
                    releaseAllVideos();
                    return;
                }
            }
            backPress();
        }
    }

    public void playVideo() {
        mCurrentState = CURRENT_STATE_PAUSE;
        onClick(mPlayControllerButton);
    }

    public void pauseVideo() {
        mCurrentState = CURRENT_STATE_PLAYING;
        onClick(mPlayControllerButton);
    }

    public boolean isPlaying() {
        return mCurrentState == CURRENT_STATE_PLAYING;
    }

    public void hideControls() {
        setAllControlsVisible(View.INVISIBLE,
                View.INVISIBLE,
                View.INVISIBLE,
                View.INVISIBLE,
                View.INVISIBLE,
                View.INVISIBLE);
    }

    @Override
    public void startWindowFullscreen() {
        MxMediaManager.getInstance().mIsShowBottomProgressBar = mIsShowBottomProgressBar;
        super.startWindowFullscreen();
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
    public void resetProgressAndTime() {
        super.resetProgressAndTime();
        mBottomProgressBar.setProgress(0);
        mBottomProgressBar.setSecondaryProgress(0);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        super.onStartTrackingTouch(seekBar);
        cancelDismissControlViewTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        startDismissControlViewTimer();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_video_layout_mobile;
    }

    @Override
    protected void initAttributeSet(Context context, AttributeSet attrs) {
        if (attrs == null) {
            mIsShowBottomProgressBar = MxMediaManager.getInstance().mIsShowBottomProgressBar;
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

    @Override
    protected boolean isShowNetworkStateDialog() {
        if (!mPlayUrl.startsWith("file") && !MxUtils.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
            showWifiDialog();
            return true;
        }
        return false;
    }

    public long getVideoPosition() {
        return MxMediaManager.getInstance().getPlayer().getCurrentPosition();
    }

    private void showWifiDialog() {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        preparePlayVideo();
                        WIFI_TIP_DIALOG_SHOWED = true;
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();*/
    }


    @Override
    protected void showProgressDialog(float deltaX, String seekTime,
                                      int seekTimePosition, String totalTime, int totalTimeDuration) {
        if (mProgressDialog == null) {
            View localView = View.inflate(getContext(), R.layout.mx_progress_dialog, null);
            mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
            //mDialogProgressBar = ((PreviewSeekBar) localView.findViewById(R.id.duration_progressbar));
            mDialogSeekTime = ((TextView) localView.findViewById(R.id.video_current));
            mDialogTotalTime = ((TextView) localView.findViewById(R.id.video_duration));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mProgressDialog = new Dialog(getContext(), R.style.mx_style_dialog_progress);
            mProgressDialog.setContentView(localView);
            if (mProgressDialog.getWindow() != null) {
                mProgressDialog.getWindow().addFlags(Window.FEATURE_ACTION_BAR);
                mProgressDialog.getWindow().addFlags(32);
                mProgressDialog.getWindow().addFlags(16);
                mProgressDialog.getWindow().setLayout(-2, -2);
            }
            WindowManager.LayoutParams params = mProgressDialog.getWindow().getAttributes();
            params.gravity = 49;
            params.y = getResources().getDimensionPixelOffset(R.dimen.mx_progress_dialog_margin_top);
            params.width = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.mx_mobile_dialog_width);
            mProgressDialog.getWindow().setAttributes(params);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        long seekedTime = Math.abs(getCurrentPositionWhenPlaying() - seekTimePosition);
        String seekTimeText = "(" + MxUtils.stringForTime(seekedTime) + ") " + seekTime;
        mDialogSeekTime.setText(seekTimeText);
        mDialogTotalTime.setText(String.format(" / %s", totalTime));
        mDialogProgressBar.setProgress(totalTimeDuration <= 0 ? 0 : (seekTimePosition * 100 / totalTimeDuration));
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.mx_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.mx_backward_icon);
        }
    }

    @Override
    protected void showVolumeDialog(float v, int volumePercent) {
        if (mVolumeDialog == null) {
            View localView = View.inflate(getContext(), R.layout.mx_mobile_volume_dialog, null);
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            mVolumeDialog = new Dialog(getContext(), R.style.mx_style_dialog_progress);
            mVolumeDialog.setContentView(localView);
            if (mVolumeDialog.getWindow() != null) {
                mVolumeDialog.getWindow().addFlags(8);
                mVolumeDialog.getWindow().addFlags(32);
                mVolumeDialog.getWindow().addFlags(16);
                mVolumeDialog.getWindow().setLayout(-2, -2);
            }
            WindowManager.LayoutParams params = mVolumeDialog.getWindow().getAttributes();
            params.gravity = 49;
            params.y = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.mx_volume_dialog_margin_top);
            params.width = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.mx_mobile_dialog_width);
            mVolumeDialog.getWindow().setAttributes(params);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }
        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    @Override
    protected void showBrightnessDialog(float v, int brightnessPercent) {
        if (mBrightnessDialog == null) {
            View localView = View.inflate(getContext(), R.layout.mx_mobile_brightness_dialog, null);
            mDialogBrightnessProgressBar = ((ProgressBar) localView.findViewById(R.id.brightness_progressbar));
            mBrightnessDialog = new Dialog(getContext(), R.style.mx_style_dialog_progress);
            mBrightnessDialog.setContentView(localView);
            if (mBrightnessDialog.getWindow() != null) {
                mBrightnessDialog.getWindow().addFlags(8);
                mBrightnessDialog.getWindow().addFlags(32);
                mBrightnessDialog.getWindow().addFlags(16);
                mBrightnessDialog.getWindow().setLayout(-2, -2);
            }
            WindowManager.LayoutParams params = mBrightnessDialog.getWindow().getAttributes();
            params.gravity = 49;
            params.y = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.mx_volume_dialog_margin_top);
            params.width = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.mx_mobile_dialog_width);
            mBrightnessDialog.getWindow().setAttributes(params);
        }
        if (!mBrightnessDialog.isShowing()) {
            mBrightnessDialog.show();
        }
        mDialogBrightnessProgressBar.setProgress(brightnessPercent);
    }

    @Override
    protected void showLockDialog() {
        if (mLockDialog == null) {
            View localView = View.inflate(getContext(), R.layout.mx_lock_dialog, null);
            mDialogLockView = ((ImageButton) localView.findViewById(R.id.mx_lock));
            mDialogLockView.setImageDrawable(new IconicsDrawable(getContext())
                    .icon(mLocked ? FontAwesome.Icon.faw_lock : FontAwesome.Icon.faw_unlock).sizeDp(24));
            mDialogLockView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLocked = !mLocked;
                    if (mLocked)
                        mDialogLockView.setImageDrawable(new IconicsDrawable(getContext())
                                .icon(FontAwesome.Icon.faw_lock).sizeDp(24));
                    else
                        mDialogLockView.setImageDrawable(new IconicsDrawable(getContext())
                                .icon(FontAwesome.Icon.faw_unlock).sizeDp(24));

                    Log.v("ShowLockDialog", "mLocked is " + mLocked);
                }
            });
            localView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLocked = !mLocked;
                    if (mLocked)
                        mDialogLockView.setImageDrawable(new IconicsDrawable(getContext())
                                .icon(FontAwesome.Icon.faw_lock).sizeDp(24));
                    else
                        mDialogLockView.setImageDrawable(new IconicsDrawable(getContext())
                                .icon(FontAwesome.Icon.faw_unlock).sizeDp(24));

                    Log.v("ShowLockDialog", "mLocked is " + mLocked);
                }
            });
            mLockDialog = new Dialog(getContext(), R.style.mx_style_dialog_progress);
            mLockDialog.setContentView(localView);
            if (mLockDialog.getWindow() != null) {
                mLockDialog.getWindow().addFlags(8);
                //mLockDialog.getWindow().addFlags(32);
                //mLockDialog.getWindow().addFlags(16);
                mLockDialog.getWindow().setLayout(-2, -2);
            }
            WindowManager.LayoutParams params = mLockDialog.getWindow().getAttributes();
            params.gravity = Gravity.END | Gravity.TOP;
            params.y = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.mx_volume_dialog_margin_top) / 2;
            params.width = getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.mx_volume_dialog_margin_top) / 2;
            mLockDialog.getWindow().setAttributes(params);
        }
        if (!mLockDialog.isShowing()) {
            mLockDialog.show();
        }
    }

    @Override
    protected void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
    }

    @Override
    protected void dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
        }
    }

    @Override
    protected void dismissLockDialog() {
        if (mLockDialog != null) {
            mLockDialog.dismiss();
        }
    }

    @Override
    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
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

                            Animation fadeIn = new AlphaAnimation(0, 1);
                            fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                            fadeIn.setDuration(1000);

                            Animation fadeOut = new AlphaAnimation(1, 0);
                            fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                            fadeOut.setStartOffset(1000);
                            fadeOut.setDuration(1000);
                            if (mBottomContainer.getVisibility() == View.VISIBLE)
                                mBottomContainer.startAnimation(fadeOut);
                            if (mTopContainer.getVisibility() == View.VISIBLE)
                                mTopContainer.startAnimation(fadeOut);
                            if (mPlayControllerButton.getVisibility() == View.VISIBLE)
                                mPlayControllerButton.startAnimation(fadeOut);

                            dismissLockDialog();

                            mBottomContainer.setVisibility(View.INVISIBLE);
                            mTopContainer.setVisibility(View.INVISIBLE);
                            mPlayControllerButton.setVisibility(View.INVISIBLE);
                            if (mCurrentScreen != SCREEN_WINDOW_TINY && mIsShowBottomProgressBar) {
                                mBottomProgressBar.startAnimation(fadeIn);
                                mBottomProgressBar.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        }
    }

    public interface UIStatusChangeListener {
        void onUIChange(Mode mode);
    }
}
