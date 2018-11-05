package hb.xvideoplayer;

public interface MxPlayerListener {
    void onComplete();

    void onPrepared();
    void onStarted();
    void onStopped();
    void onBackPress();
}
