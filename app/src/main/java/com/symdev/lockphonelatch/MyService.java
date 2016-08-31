package com.symdev.lockphonelatch;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

    private static BroadcastReceiver ScreenOffReceiver;
    private static BroadcastReceiver ScreenOnReceiver;
    public static String s_accountid;
    public static int cont;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            if(cont==0){
                if(intent.getStringExtra("accountid") != null) {
                    s_accountid = intent.getStringExtra("accountid");
                    Log.d("Myservice", "En onStartCommand Accountid: " + s_accountid);
                    cont++;
                }

            }
        }


        Log.d("MyService","onstarcommand cont: "+ cont);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d("Myservice","Paso por onBind");
        return null;
    }

    @Override
    public void onCreate()
    {
        Log.d("Myservice","Paso por oncreate cont:  "+cont);
        //ccc
        cont=0;
        registerScreenOffReceiver();
        registerScreenOnReceiver();
    }



    @Override
    public void onDestroy()
    {
        Log.d("Myservice","Paso por onDestroy");
        unregisterReceiver(ScreenOffReceiver);
        unregisterReceiver(ScreenOnReceiver);
        ScreenOffReceiver = null;
        ScreenOnReceiver = null;
    }

    private void registerScreenOnReceiver()
    {
        ScreenOnReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d("MyService", "ACTION_SCREEN_ON Accountid: "+ s_accountid);
                MainActivity act = new MainActivity();
                Context context1 = MyApplication.getAppContext();
                SharedPreferences prefs =  context1.getSharedPreferences("preferences",Context.MODE_PRIVATE);
                s_accountid = prefs.getString("pref_accountid", "no_existe_account_id");
                if(act.checkPairApp(s_accountid)){
                    act.checkLatchAndLockScreen();
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        registerReceiver(ScreenOnReceiver, filter);
    }
    private void registerScreenOffReceiver()
    {
        ScreenOffReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d("MyService", "ACTION_SCREEN_OFF");
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(ScreenOffReceiver, filter);
    }
}