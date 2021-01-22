package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;

public class ScreenTwoFragment extends Fragment {

    private static final String TAG = "MSI_ScreenTwoFragment";
    private MainActivity ma;
    private TextView  qiVariableValue;

    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentId = R.layout.fragment_two;
        this.ma = (MainActivity) getActivity();
        if(ma != null){
            Integer themeId = ma.getThemeId();
            if(themeId != null){
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                return localInflater.inflate(fragmentId, container, false);
            }else{
                return inflater.inflate(fragmentId, container, false);
            }
        }else{
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        qiVariableValue = view.findViewById(R.id.two_qi_variable);
        view.findViewById(R.id.two_button_set_A).setOnClickListener((v) -> {
                ma.setQiVariable("qiVariable","Pepper"); //set the variable in the qiChat
                setTextQiVariableValue(getString(R.string.pepper)); //updates the UI displaying the variable
                });
        view.findViewById(R.id.two_button_set_B).setOnClickListener((v) -> {
                ma.setQiVariable("qiVariable","Nao");
                setTextQiVariableValue(getString(R.string.nao));
        });
        view.findViewById(R.id.two_button_reset).setOnClickListener((v) ->
                ma.setFragment(new MainFragment()));
        view.findViewById(R.id.two_button_frag_one).setOnClickListener((v) ->
                ma.setFragment(new ScreenOneFragment()));
        ma.getCurrentChatBot().variables.get("qiVariable").async().getValue().andThenConsume(
                value -> setTextQiVariableValue(value));
    }

    public void setTextQiVariableValue(String value){
        ma.runOnUiThread(() -> qiVariableValue.setText(value));
    }
}
