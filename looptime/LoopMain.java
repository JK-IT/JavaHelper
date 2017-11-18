package jkit.looptime;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

public class LoopMain extends AppCompatActivity implements TimePickerFragController.onTimePickedListener {

    protected static final String TAG = "Loop Main Debug";
    private PackageManager pm;
    //private Button mSetButton;
    protected TextView mTimeViewTitle;
    protected TextView mTimeDisplay;
    private static CountDownTimer mCountDownTimer;
    private static AlarmManager sAlarmManager;
    private PendingIntent pendingIntent;
    protected static final int SYSTEM_ALERT_GRANTED = 5;
    //private int hourFromFrag = 0 ;
    //private int minFromFrag = 0;

    /*------------  start Create definition of method --------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        pm = getPackageManager();
        sAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        mTimeViewTitle = (TextView)findViewById(R.id.displayTitle);
        mTimeDisplay = (TextView)findViewById(R.id.timeDisplay);
        Button mSetButton =  (Button)findViewById(R.id.setTimeButt);

             //create pendingIntent for alarm
        Intent ringIntent = RingtoneService.newIntent(getApplicationContext());
        pendingIntent = PendingIntent.getService(LoopMain.this, 0, ringIntent, 0);

        mSetButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view)
    {
        final Calendar c = Calendar.getInstance();
        final int hourInLoop = c.get(Calendar.HOUR_OF_DAY);
        final int minInLoop = c.get(Calendar.MINUTE);
        DialogFragment timeDialogFrag = TimePickerFragController.newInstance(hourInLoop,minInLoop);
        timeDialogFrag.show(getSupportFragmentManager(), "Time Picker");
            }
        });
        /*  ======= This is for fragment container ===========*/
        FragmentManager fm = getSupportFragmentManager();
        //find fragment in the view
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null)
        {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
        getDefaultPackage();

        /*      =======Check for Permission granted=========*/

    }

    //override response from permission granted

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected LoopFragController createFragment()
    {
        return LoopFragController.newInstance();
    }

    @Override
    public void onBackPressed() {
        getApplicationContext().stopService(RingtoneService.newIntent(getApplicationContext()));
        return;
    }

    /*this is the interface u implement so u can exchange data with ur dialog fragment and
    u implement whatever needed here
     */
    @Override
    public void onTimePicked(int hour, int min)
    {

        Calendar c = Calendar.getInstance();
        long diff = CalculateTimeDiff(hour, min, c);
        StartCountdown(diff);
        mTimeViewTitle.setText(R.string.time_run_title);
        Log.d(TAG, "This is what u input " + hour + " " + min);
        setAlarm(c);

    }

    protected long CalculateTimeDiff(int hourIn,int Minin, Calendar c)
    {
        //if user set it to 24'clock in 24 hour format, u have to add the day so the calculate will be acurate
        //before adding a day u need to check current hour, if it is 0 then no need to add
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
       /* if(currentHour == 0)
        {
            c.set(Calendar.HOUR_OF_DAY, hourIn);
            c.set(Calendar.MINUTE, Minin);
        } else */
                ///now check input and adjust time
        if(((hourIn < c.get(Calendar.HOUR_OF_DAY)) & currentHour != 0)
                || Minin < c.get(Calendar.MINUTE))
        {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hourIn);
        c.set(Calendar.MINUTE, Minin);
        return c.getTime().getTime() - System.currentTimeMillis();
    }

    protected void StartCountdown(long diff)
    {
        //check if a coundonw timer already running,
        //cancel then set up a new countdown
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer( diff, 1000) {
            @Override
            public void onTick(long l) {
                long hourLeft = TimeUnit.MILLISECONDS.toHours(l) % 24;
                long minLeft = TimeUnit.MILLISECONDS.toMinutes(l) % 60; // = (l/1000 *60) %60
                long secLeft = TimeUnit.MILLISECONDS.toSeconds(l)%60; //=(l / 1000) % 60;
                mTimeDisplay.setText(hourLeft + " : " + minLeft + " : " + secLeft);
            }

            @Override
            public void onFinish() {
                alertFinish();
            }
        };
        mCountDownTimer.start();
    }

    protected void cancelAlarm()
    {
        if(sAlarmManager != null)
            sAlarmManager.cancel(pendingIntent);
    }
    protected void setAlarm(Calendar calendar)
    {
        cancelAlarm();
        Calendar c = calendar;
        //now u gonna set alarm
        if(Build.VERSION.SDK_INT >= 19)
            sAlarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        else
            sAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

        /*  ----Tempting to set built in alarm------
        Intent setBuiltInAlarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        setBuiltInAlarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        setBuiltInAlarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, min);
        setBuiltInAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //remember to use context to set as a place that call this activity
        startActivity(setBuiltInAlarmIntent);
        */
    }

    protected void alertFinish()
    {
        Dialog dialog = new AlertDialog.Builder(getApplicationContext())
            .setTitle("Time Up")
            .setMessage("Congratulation!!! You can now get back to normal activity or keep using the app.")
            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getApplicationContext().stopService(RingtoneService.newIntent(getApplicationContext()));
                }
            })
            .setCancelable(false).create();

        dialog.show();
    }

    private void getDefaultPackage()
    {
         /* =====testing of retrieving the name of all default package in system----*/
        ///u can clear the default setting with this ----LOL
        List<IntentFilter> outInfilList = new Vector<>();
        List<ComponentName> outComponentNameList = new Vector<>();
        //this will retrieve all the default activity include launcher
        pm.getPreferredActivities(outInfilList, outComponentNameList, null);
        String currDefLauncher  = null;
        for(int j = 0; j < outInfilList.size(); j++ )
        {
            //componentList[j] == infilList[j] refers to the same package
            if(outInfilList.get(j).hasCategory(Intent.CATEGORY_HOME)) {
                //Log.d(TAG, outComponentNameList.get(j).getClassName());
                currDefLauncher = outComponentNameList.get(j).getClassName();
            }
        }
        /* for(int j = 0; j < outComponentNameList.size(); j++){
            Log.d(TAG,  outComponentNameList.get(j).getClassName());
            if(outComponentNameList.get(j).getClassName().contains("Launcher"))
            {
                currDefLauncher = outComponentNameList.get(j).getClassName();
            }
        }*/
        Log.d(TAG,  "This is the current default Launcher " + currDefLauncher);
    }

    private void checkPermission()
    {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(LoopMain.this, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, SYSTEM_ALERT_GRANTED);
        }
    }
}
