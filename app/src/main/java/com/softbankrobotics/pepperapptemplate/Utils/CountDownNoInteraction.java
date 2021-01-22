package com.softbankrobotics.pepperapptemplate.Utils;

import android.os.CountDownTimer;
import androidx.fragment.app.Fragment;
import android.util.Log;

import com.softbankrobotics.pepperapptemplate.MainActivity;

public class CountDownNoInteraction extends CountDownTimer {

    private String TAG = "MSI_NoInteraction";
    private Fragment fragment;
    private MainActivity mainActivity;

    public CountDownNoInteraction(MainActivity mainActivity, Fragment fragmentToSet, long millisUtilEnd, long countDownInterval) {
        super(millisUtilEnd, countDownInterval);
        this.fragment = fragmentToSet;
        this.mainActivity = mainActivity;
    }


    @Override
    public void onTick(long millisUntilFinished) {
        //Log.d(TAG,"Millis until end : " + millisUntilFinished);
    }

    @Override
    public void onFinish() {
        Log.d(TAG, "Timer Finished");
        mainActivity.setFragment(fragment);
    }

    public void reset() {
        Log.d(TAG, "Timer Reset");
        super.cancel();
        super.start();
    }
}
