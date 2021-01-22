package com.softbankrobotics.pepperapptemplate;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.TopicStatus;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.softbankrobotics.pepperapptemplate.Executors.FragmentExecutor;
import com.softbankrobotics.pepperapptemplate.Executors.VariableExecutor;
import com.softbankrobotics.pepperapptemplate.Fragments.LoadingFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.MainFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.SplashFragment;
import com.softbankrobotics.pepperapptemplate.Utils.ChatData;
import com.softbankrobotics.pepperapptemplate.Utils.CountDownNoInteraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MSI_MainActivity";
    //topicNames needs to be updated wih the topics names of the topics in the raw resource dir
    private final List<String> topicNames = Arrays.asList("main", "screenone", "screentwo", "concepts");
    private FragmentManager fragmentManager;
    private QiContext qiContext;
    private ChatData currentChatBot, englishChatBot;
    private String currentFragment, currentTopicName;
    private TopicStatus currentTopicStatus;
    private CountDownNoInteraction countDownNoInteraction;
    private HumanAwareness humanAwareness;
    private android.content.res.Configuration config;
    private Resources res;
    private Future<Void> chatFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);
        countDownNoInteraction = new CountDownNoInteraction(this, new SplashFragment(),
                30000, 10000);
        countDownNoInteraction.start();
        updateLocale("en");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "test");

    }

    /**
     * Sets the locale for this activity
     *
     * @param strLocale the string used to build the new locale
     */

    private void updateLocale(String strLocale) {
        Locale locale = new Locale(strLocale);
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.d(TAG, "onRobotFocusedGained");
        this.qiContext = qiContext;
        englishChatBot = new ChatData(this, qiContext, new Locale("en"), topicNames, true);
        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("FragmentExecutor", new FragmentExecutor(qiContext, this));
        executors.put("VariableExecutor", new VariableExecutor(qiContext, this));
        englishChatBot.setupExecutors(executors);
        englishChatBot.setupQiVariable("qiVariable");
        currentChatBot = englishChatBot;
        currentChatBot.chat.async().addOnStartedListener(() -> {
            setQiVariable("qiVariable", "Pepper"); // this is done here because the chatBot needs to be running for this to work.
            runOnUiThread(() -> {
                setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS); // Disable overlay mode for the rest of the app.
                setFragment(new MainFragment());
            });
        });
        currentChatBot.chat.async().addOnNormalReplyFoundForListener(input -> {
            countDownNoInteraction.reset();
        });
        chatFuture = currentChatBot.chat.async().run();
        humanAwareness = getQiContext().getHumanAwareness();
        humanAwareness.async().addOnEngagedHumanChangedListener(engagedHuman -> {
            if (getFragment() instanceof SplashFragment) {
                if (engagedHuman != null) {
                    setFragment(new MainFragment());
                }
            } else {
                countDownNoInteraction.reset();
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        humanAwareness.async().removeAllOnEngagedHumanChangedListeners();
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "onRobotFocusRefused");
    }

    @Override
    protected void onDestroy() {
        countDownNoInteraction.cancel();
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        countDownNoInteraction.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY); // We don't want to see the speech bar while loading
        this.setFragment(new LoadingFragment());
    }

    @Override
    public void onUserInteraction() {
        if (getFragment() instanceof SplashFragment) {
            setFragment(new MainFragment());
            countDownNoInteraction.start();
        } else {
            countDownNoInteraction.reset();
        }
    }

    /**
     * updates the value of the qiVariable
     *
     * @param variableName the name of the variable
     * @param value        the value that needs to be set
     */

    public void setQiVariable(String variableName, String value) {
        Log.d(TAG, "size va : " + currentChatBot.variables.size());
        currentChatBot.variables.get(variableName).async().setValue(value);
    }

    public ChatData getCurrentChatBot() {
        return currentChatBot;
    }

    public QiContext getQiContext() {
        return qiContext;
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Fragment getFragment() {
        return fragmentManager.findFragmentByTag("currentFragment");
    }

    /**
     * Change the fragment displayed by the placeholder in the main activity, and goes to the
     * bookmark init in the topic assigned to this fragment
     *
     * @param fragment the fragment to display
     */

    public void setFragment(Fragment fragment) {
        currentFragment = fragment.getClass().getSimpleName();
        String topicName = currentFragment.toLowerCase().replace("fragment", "");
        if (!(fragment instanceof LoadingFragment) && !(fragment instanceof SplashFragment)) {
            this.currentChatBot.goToBookmarkNewTopic("init", topicName);
        }
        Log.d(TAG, "Transaction for fragment : " + fragment.getClass().getSimpleName());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left,
                R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

