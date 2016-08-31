package com.symdev.lockphonelatch;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.elevenpaths.latch.Latch;
import com.elevenpaths.latch.LatchResponse;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String LATCH_APP_ID = "app_id";
    private static final String LATCH_SECRET = "secret";
    private static final String AT_FIRST = "atfirst";
    private static final String WITH_PAIR = "withpair";
    private static final String ADMIN_ACTIVE= "adminactive";
    private static final String ADMIN_DEACTIVE= "admindeactive";
    private static final String NOT_EXISTS_ACCOUNT_ID = "no_existe_account_id";
    static final int RESULT_ENABLE = 1;
    private static String accountId;
    private Button btnpair,btnunpair,btnenable,btndisable;
    private EditText edittoken;

    static ComponentName compName;
    static DevicePolicyManager deviceManager;
    ActivityManager activityManager;
    static WindowManager wm;
    static Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d("MainActivity","Oncreate");

        deviceManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, AdminReceiver.class);
        wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        window = getWindow();

        edittoken = (EditText)findViewById(R.id.edittoken);
        btnpair = (Button)findViewById(R.id.btnpair);
        btnunpair = (Button)findViewById(R.id.btnunpair);
        btnenable = (Button)findViewById(R.id.btnenable);
        btndisable = (Button)findViewById(R.id.btndisable);;
        btnpair.setOnClickListener(this);
        btnunpair.setOnClickListener(this);
        btnenable.setOnClickListener(this);
        btndisable.setOnClickListener(this);

        //Get accountId if exists
        SharedPreferences prefs =  getSharedPreferences("preferences",Context.MODE_PRIVATE);
        accountId = prefs.getString("pref_accountid", NOT_EXISTS_ACCOUNT_ID);

        //Enabled buttons
        if(checkPairApp(accountId)){
            enabledDisabledButtons(WITH_PAIR);
        }else{
            enabledDisabledButtons(AT_FIRST);
        }

    }

    public void enabledDisabledButtons(String option){
        if(option.equals(AT_FIRST)){
            btnpair.setEnabled(true);
            btnunpair.setEnabled(false);
            btnenable.setEnabled(false);
            btndisable.setEnabled(false);
        }
        else if(option.equals(WITH_PAIR)){
            btnpair.setEnabled(false);
            btnunpair.setEnabled(true);
            SharedPreferences prefs =  getSharedPreferences("preferences",Context.MODE_PRIVATE);
            boolean permissionAdmin = prefs.getBoolean("permissionAdmin",true);
            if(permissionAdmin){
                btnenable.setEnabled(false);
                btndisable.setEnabled(true);
            }else{
                btnenable.setEnabled(true);
                btndisable.setEnabled(false);
            }
        }
        else if(option.equals(ADMIN_ACTIVE)){
            btnenable.setEnabled(false);
            btndisable.setEnabled(true);
        }
        else if(option.equals(ADMIN_DEACTIVE)){
            btnenable.setEnabled(true);
            btndisable.setEnabled(false);
        }

    }

    @Override
    public void onClick(View v) {
        if(v == btnpair){
            String token = edittoken.getText().toString();
            new PairTaskAsync().execute(token);
        }
        if(v == btnunpair){
            new UnPairTaskAsync().execute();

            enabledDisabledButtons(AT_FIRST);

            //Disable permissions
            compName = new ComponentName(this, AdminReceiver.class);
            deviceManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            deviceManager.removeActiveAdmin(compName);

            SharedPreferences prefs = getSharedPreferences("preferences",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("permissionAdmin",false);
            editor.commit();
        }
        if(v == btnenable){
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable administrator permissions to the application");
            startActivityForResult(intent, RESULT_ENABLE);
            enabledDisabledButtons(ADMIN_ACTIVE);

            SharedPreferences prefs = getSharedPreferences("preferences",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("permissionAdmin",true);
            editor.commit();
        }
        if(v == btndisable){
            deviceManager.removeActiveAdmin(compName);
            enabledDisabledButtons(ADMIN_DEACTIVE);
            SharedPreferences prefs = getSharedPreferences("preferences",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("permissionAdmin",false);
            editor.commit();
        }
    }

    public static boolean lockwithPIN(){
        ComponentName compName1;
        DevicePolicyManager deviceManager1;
        Context context1 = MyApplication.getAppContext();
        deviceManager1 = (DevicePolicyManager)context1.getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName1 = new ComponentName(context1, AdminReceiver.class);
        deviceManager1.setPasswordQuality(compName1,DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        deviceManager1.setPasswordMinimumLength(compName1, 5);
        boolean result = deviceManager1.resetPassword("123456",DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        Log.i("lockwithPIN","El bloqueo con password ha resultado correcto: " + result);
        return result;
    }

    public static boolean unlockwithPIN(){

        ComponentName compName1;
        DevicePolicyManager deviceManager1;
        Context context1 = MyApplication.getAppContext();
        deviceManager1 = (DevicePolicyManager)context1.getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName1 = new ComponentName(context1, AdminReceiver.class);
        deviceManager1.setPasswordMinimumLength(compName1, 0);
        boolean result = deviceManager1.resetPassword("", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        Log.i("unlockwithPIN","El desbloqueo con password ha resultado correcto: " + result);
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static boolean pair(String token){
        boolean pairOk = false;
        Latch latch = new Latch(LATCH_APP_ID,LATCH_SECRET);
        LatchResponse response = latch.pair(token);

        if(response != null){
            if(response.getData() != null){
                Log.i("pair","JSON: "+ response.toJSON().toString());
                accountId = response.getData().get("accountId").getAsString();
                pairOk = true;
                Log.i("pair():PAREADO","Correcto");
            }else{
                pairOk = false;
                Log.i("pair():PAREADO","Incorrecto");
            }
        }

        return pairOk;
    }//fin pair

    public static boolean unpair(){
        boolean unpairOk = false;

        Context context1 = MyApplication.getAppContext();
        SharedPreferences prefs =  context1.getSharedPreferences("preferences",Context.MODE_PRIVATE);
        accountId = prefs.getString("pref_accountid", NOT_EXISTS_ACCOUNT_ID);

        Latch latch = new Latch(LATCH_APP_ID,LATCH_SECRET);
        LatchResponse response = latch.unpair(accountId);

        if(response != null && response.getError() == null){
            Log.i("unpair():","Despareado satisfactorio");
            unpairOk = true;
        }else{
            Log.i("unpair():","La app no se ha despareado");
            unpairOk = false;
        }

        return unpairOk;
    }

    public class PairTaskAsync extends AsyncTask<String,Void,Boolean> {
        boolean pairOk = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            try{
                pairOk = pair(strings[0]);
            }catch (Exception e){
                Log.e("Error PairAsynTask",e.getMessage());
            }

            return pairOk;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                Log.i("PairTaskAsync","onPostExecute: Pareado correcto");
                //Start service and set preferences with accountid
                enabledDisabledButtons(WITH_PAIR);
                Intent service = new Intent(getApplicationContext(), MyService.class);
                service.putExtra("accountid",accountId);

                SharedPreferences prefs = getSharedPreferences("preferences",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("pref_accountid", accountId);
                editor.commit();

                Log.d("PairAsynctask","onpostexecute accountid: "+accountId);

                getApplicationContext().startService(service);
            }else{
                Log.i("PairTaskAsync","OnPostExecute: Pareado incorrecto");
            }
        }


    } // fin asynctask

    public class UnPairTaskAsync extends AsyncTask<String,Void,Boolean>{
        boolean unpairOk = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            try{
                unpairOk = unpair();
            }catch (Exception e){
                Log.e("Error UnPairAsynTask",e.getMessage());
            }

            return unpairOk;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                // Stop service and set preferences
                SharedPreferences prefs = getSharedPreferences("preferences",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("pref_accountid", NOT_EXISTS_ACCOUNT_ID);
                editor.commit();

                Intent service = new Intent(getApplicationContext(), MyService.class);
                getApplicationContext().stopService(service);
                Log.i("OnPostExecute","Despareado correcto");
            }else{
                Log.i("OnPostExecute","Despareado incorrecto");
            }
        }


    } // fin asynctask

    public void checkLatchAndLockScreen(){
        boolean isLatchOn=false;
        try {
            CheckLatchkAsync task = new CheckLatchkAsync();
            task.execute();
            isLatchOn = task.get();
            Log.i("ckLatchLockScrTaskGet",""+task.get());
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        if(isLatchOn){
            Log.i("checkLatchAndLockScreen","Latch abierto para desbloquear la pantalla: "+isLatchOn);
            // window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }else{
            Log.i("checkLatchAndLockScreen","Latch abierto para desbloquear la pantalla: "+isLatchOn);
            //deviceManager.lockNow();
        }

    }

    public class CheckLatchkAsync extends AsyncTask<String,Void,Boolean>{
        boolean chklatch = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            try{
                chklatch =checklatch();
            }catch (Exception e){
                Log.e("ErrorCheckLatchAsynTask",e.getMessage());
            }
            Log.i("CheckLatchkAsync","Latch esta abierto: "+chklatch);
            return chklatch;
        }



        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                //Log.i("CheckLatchkAsync","Latch esta abierto: "+aBoolean);
            }else{
                // Log.i("CheckLatchkAsync","Latch esta abierto: "+aBoolean);
            }
        }


    } // fin asynctask

    public static boolean checkPairApp(String s_accountid){
        boolean isPair = false;

        Log.d("checkpairapp","Accountid pasado por variable: "+ s_accountid);
        if(!s_accountid.equals(NOT_EXISTS_ACCOUNT_ID)){
            isPair = true;
        }else{
            isPair=false;
        }
        return isPair;
    }

    public boolean checklatch(){
        boolean isLatchOn = true;

        Context context1 = MyApplication.getAppContext();
        SharedPreferences prefs =  context1.getSharedPreferences("preferences",Context.MODE_PRIVATE);
        accountId = prefs.getString("pref_accountid", NOT_EXISTS_ACCOUNT_ID);
        Log.d("checklatch","accountid: "+ accountId);

        Latch latch = new Latch(LATCH_APP_ID,LATCH_SECRET);
        LatchResponse response = latch.status(accountId);

        String status = response.getData().get("operations").getAsJsonObject().get(LATCH_APP_ID).getAsJsonObject().get("status").getAsString();
        Log.i("checklatch","Status: "+ status);
        if(status.equals("off")){
            isLatchOn = false;
            boolean result = lockwithPIN();
            Log.i("checklatch","Bloqueo con pin:" +result);
        }
        if(isLatchOn){
            boolean result = unlockwithPIN();
            Log.i("checklatch","Desbloqueo con pin:" +result);
        }else{
            Log.i("checklatch","Latch esta cerrado");

        }

        return isLatchOn;
    }

}

