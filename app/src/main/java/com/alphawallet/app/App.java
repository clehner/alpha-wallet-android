package com.alphawallet.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.alphawallet.app.walletconnect.AWWalletConnectClient;
import com.alphawallet.app.util.ReleaseTree;

import java.util.Stack;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.realm.Realm;
import timber.log.Timber;

@HiltAndroidApp
public class App extends Application
{

    @Inject
    AWWalletConnectClient awWalletConnectClient;

    private static App mInstance;
    private final Stack<Activity> activityStack = new Stack<>();

    public static App getInstance()
    {
        return mInstance;
    }

    public Activity getTopActivity()
    {
        return activityStack.peek();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
        Realm.init(this);

        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        } else
        {
            Timber.plant(new ReleaseTree());
        }

        awWalletConnectClient.init(this);
        // enable pin code for the application
//		LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
//		lockManager.enableAppLock(this, CustomPinActivity.class);
//		lockManager.getAppLock().setShouldShowForgot(false);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks()
        {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState)
            {
            }

            @Override
            public void onActivityDestroyed(Activity activity)
            {
            }

            @Override
            public void onActivityStarted(Activity activity)
            {
            }

            @Override
            public void onActivityResumed(Activity activity)
            {
                activityStack.push(activity);
            }

            @Override
            public void onActivityPaused(Activity activity)
            {
                pop();
            }

            @Override
            public void onActivityStopped(Activity activity)
            {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState)
            {
            }
        });
    }

    @Override
    public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        awWalletConnectClient.shutdown();
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        activityStack.clear();
        awWalletConnectClient.shutdown();
    }

    private void pop()
    {
        activityStack.pop();
    }
}
