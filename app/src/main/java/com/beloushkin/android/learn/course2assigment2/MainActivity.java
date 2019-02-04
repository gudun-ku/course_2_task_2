package com.beloushkin.android.learn.course2assigment2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity  implements
        ProgressService.ProgressUpdateListener {


    private ProgressBar mProgress;
    private Toast mToast;
    private Button btnStart;
    private ProgressService mProgressService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mProgressService = ((ProgressService.ProgressBinder)service).getService();
            mProgressService.registerListener(MainActivity.this);
            mProgressService.setCurrentProgress(0);
        }

        public void onServiceDisconnected(ComponentName className) {
            mProgressService = null;
        }
    };

    private void doBindService() {
        bindService(new Intent(this,
                ProgressService.class), mConnection, Context.BIND_AUTO_CREATE);

        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
                if (mProgressService != null) {
                    mProgressService.unregisterListener(this);
                }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG );
        mToast.show();
    }

    @Override
    public void onUpdate(final int value) {
        mProgress.setProgress(value);
        btnStart.setText(value + "%");
    }

    @Override
    public void onFinish() {
         btnStart.setText(getString(R.string.lbl_button_finish));
         showToast(getString(R.string.str_msg_finish));
         doUnbindService();
        btnStart.setText(getString(R.string.lbl_button_start));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circular);
        mProgress = findViewById(R.id.circularProgressbar);
        mProgress.setProgress(0);   // Main Progress
        mProgress.setSecondaryProgress(100); // Secondary Progress
        mProgress.setMax(100); // Maximum Progress
        mProgress.setProgressDrawable(drawable);

        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsBound) {
                    if (mProgressService != null) {
                        int currProgress = mProgressService.getCurrentProgress();
                        currProgress = currProgress - 50 >= 0 ? currProgress - 50 : 0;
                        mProgressService.setCurrentProgress(currProgress);
                    }
                } else {
                    doBindService();
                }
            }
        });

        doBindService();
    }

}
