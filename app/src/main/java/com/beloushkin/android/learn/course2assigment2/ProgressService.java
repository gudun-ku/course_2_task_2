package com.beloushkin.android.learn.course2assigment2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProgressService extends Service {

    public static final String TAG =
            ProgressService.class.getSimpleName();

    public interface ProgressUpdateListener {
        void onUpdate(int value);
        void onFinish();
    }

    // Handler to main thread
    private Handler handler = new Handler();

    // Binder given to clients
    private final IBinder mBinder = new ProgressBinder();

    // Progress objects
    private ScheduledExecutorService mScheduledExecutorService;

    // Listeners array - making this service a bit observable
    private final List<ProgressUpdateListener> mListeners
            = new ArrayList<ProgressUpdateListener>();

    public void registerListener(ProgressUpdateListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(ProgressUpdateListener listener) {
        mListeners.remove(listener);
    }

    private void sendUpdate(final int value) {

        // Here we have to use handler because of updating activity components
        // from another thread.
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = mListeners.size() - 1; i >= 0; i--) {
                    mListeners.get(i).onUpdate(value);
                }
            }
        });

    }

    private void stopAndSendFinish() {
        // Stop the executors
        mScheduledExecutorService.shutdownNow();
        mIsRunning = false;
        // Here we again have to use handler because of updating activity components
        // from another thread.
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = mListeners.size() - 1; i >= 0; i--) {
                    mListeners.get(i).onFinish();
                }
            }
        });
    }

    private int currentProgress;
    private int secondaryProgress = 100;
    private int maxProgress = 100;
    private boolean mIsRunning;

    public boolean isRunning() {
        return  mIsRunning;
    }

    public int getSecondaryProgress() {
        return secondaryProgress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public void decreaseProgressByFiftyPercent() {
        int currProgress = getCurrentProgress();
        currProgress = currProgress - 50 >= 0 ? currProgress - 50 : 0;
        setCurrentProgress(currProgress);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class ProgressBinder extends Binder {
        ProgressService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ProgressService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // when we press start button, service starts again, so reset progress.
        currentProgress = 0;

        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                if (currentProgress >= 100) {
                    stopAndSendFinish();
                } else {
                    currentProgress += 5;
                    sendUpdate(currentProgress);
                }


            }
        }, 1000, 200, TimeUnit.MILLISECONDS);
        mIsRunning = true;
        return START_NOT_STICKY;
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        //!!! stop the executor service!!!
        mScheduledExecutorService.shutdownNow();
        mIsRunning = false;
    }
}
