package org.dev.atang020.lockblock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.widget.TextView;
import android.os.CountDownTimer;
import java.util.concurrent.TimeUnit;
import android.widget.EditText;
import android.util.Log;
import android.app.ActionBar;
import android.app.Activity;
import android.view.View.OnClickListener;
import android.app.ActivityManager;
import android.view.WindowManager;
import android.view.Window;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;

public class MainActivity extends Activity implements SensorEventListener{



    //NOTIFICATION
    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;

    private long timeLeft;

    private static CountDownTimer countDownTimer;
    private boolean timerHasStarted = false;
    public TextView timerText;
    public TextView text1;
    private final long interval = 1 * 1000;
    MediaPlayer siren;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Camera camera;
    protected Vibrator vibe;
    Parameters params;

    EditText editTime1;
    Button startButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Custom Font
        TextView title = (TextView)findViewById(R.id.textView);
        TextView title1 = (TextView) findViewById(R.id.textView1);

        // Create custom font
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");

        // Set the custom font of the catchphrase
        title.setTypeface(customFont);
        title1.setTypeface(customFont);

        Button b1 = (Button) findViewById(R.id.button);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAirplaneMode();
            }
        });

        /////////////////////////////////////////
        ///Notifications
        /////////////////////////////////////////
        notification = new NotificationCompat.Builder(this);
        notification.setOngoing(true);
        notification.setAutoCancel(true); //deletes icon in system status bar


        editTime1 = (EditText) findViewById(R.id.editTime1);

        startButton = (Button) findViewById(R.id.startButton);
        timerText = (TextView) this.findViewById(R.id.timer);

        // Set the custom font of the timer
        timerText.setTypeface(customFont);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        vibe = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        siren = MediaPlayer.create(this, R.raw.siren);

        final String FORMAT = "%02d:%02d:%02d";
        startButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {


                // get the name from edittext and storing into string variable
                long timeVal = Long.parseLong(editTime1.getText().toString());

                if (!timerHasStarted) {
                    countDownTimer = new MyCountDownTimer(timeVal, interval);
                    timerText.setText(timerText.getText() + String.valueOf(timeVal / 1000));
                    countDownTimer.start();
                    timerHasStarted = true;
                    timerText.setVisibility(View.VISIBLE);
                    editTime1.setVisibility(View.INVISIBLE);
                    startButton.setText("Stop");
                } else {
                    testQuestion();
                    /*
                    countDownTimer.cancel();
                    if(siren.isPlaying())
                        siren.pause();
                    turnOffFlash();
                    timerHasStarted = false;
                    editTime1.setVisibility(View.VISIBLE);
                    timerText.setVisibility(View.INVISIBLE);
                    startButton.setText("Start");
                    */
                }
            }


            class MyCountDownTimer extends CountDownTimer {
                public MyCountDownTimer(long timeVal, long interval) {
                    super(timeVal * 60000, interval);
                }

                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeft = millisUntilFinished;
                    timerText.setText("Time Remaining: " + String.format(FORMAT, TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }

                @Override
                public void onFinish() {
                    timerHasStarted = false;
                    timerText.setText("Completed");
                }
            }

        });
    }


    //clear text when editTime1 is selected
    public void clear(View v) {
        editTime1.setText("");

    }

    //disable back button
    @Override
    public void onBackPressed(){
        //display message

        if(timerHasStarted){

        }
        else{
            super.onBackPressed();
        }
    }



//    //disable options button
//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        startActivity(getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
//    }
    @Override
    protected void onPause() {

        if(timerHasStarted){
            super.onPause();
            ActivityManager activityManager = (ActivityManager) getApplicationContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.moveTaskToFront(getTaskId(), 0);
        }else{
            super.onPause();
        }

    }


    /////////////////////////////////////////
    ///SENSOR, VIBRATION, SOUND AND LIGHTS
    /////////////////////////////////////////
    @Override
    public void onSensorChanged (SensorEvent event) {
        Log.e("TIMER sensor", Boolean.toString(timerHasStarted));
        if(timerHasStarted) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.e("VALUE x ", Float.toString(x));
            Log.e("VALUE y", Float.toString(y));
            Log.e("VALUE z", Float.toString(z));

            if (z < 9.7) {
                vibe.vibrate(500);
                getCamera();
                blinkFlash(true);
                siren.setLooping(true);
                siren.start();
            } else {
                vibe.cancel();
                getCamera();
                turnOffFlash();
                Log.e("JINX: " , Boolean.toString(siren.isPlaying()));
                if (siren.isPlaying())
                    siren.pause();
            }
        }
    }

    //sensor stuff
    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
        //do something if accuracy changed
    }


    @Override
    protected void onResume(){
        super.onResume();
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //camera
    private void getCamera(){
        if(camera == null){
            try{
                camera = Camera.open();
                params = camera.getParameters();

            } catch(RuntimeException e) {
                Log.e("Camera failed to open", e.getMessage());
            }
        }
    }

    //flash blink
    private void blinkFlash(boolean isOn){
        turnOffFlash();
        if(isOn){
            turnOnFlash();
            turnOffFlash();
        }
        else
            turnOffFlash();
    }

    //turns on flash
    private void turnOnFlash(){
        if(camera == null || params == null)
            return;
        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();
    }

    //turns off flash
    private void turnOffFlash(){
        if(camera == null || params == null)
            return;
        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(camera != null){
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        siren.release();
        siren = null;
    }
    /////////////////////////////////////////
    ///Notifications
    /////////////////////////////////////////
    public void buttonClicked(View view) {

        //build the notification
        notification.setSmallIcon(R.drawable.ic_timer_white_48dp);
        //notification.setTicker("This is ticker");
        //notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("LockBlock");

        final String FORMAT = "%02d:%02d";
        final String FORMAT1 = "%s and %s";
        final String FORMAT2 = "%s";
        if(TimeUnit.MILLISECONDS.toHours(timeLeft) > 1 && TimeUnit.MILLISECONDS.toMinutes(timeLeft) != 0){
            String timeString = (String.format(FORMAT1,
                    Long.toString(TimeUnit.MILLISECONDS.toHours(timeLeft)) + " hours",
                    Long.toString(TimeUnit.MILLISECONDS.toMinutes(timeLeft) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(timeLeft))) + " minutes left"));
            notification.setContentText(timeString);
        }
        else if(TimeUnit.MILLISECONDS.toHours(timeLeft) > 1 && TimeUnit.MILLISECONDS.toMinutes(timeLeft) == 0) {
            String timeString = (String.format(FORMAT1,
                    Long.toString(TimeUnit.MILLISECONDS.toHours(timeLeft)) + " hours left"));
            notification.setContentText(timeString);
        }
        else if(TimeUnit.MILLISECONDS.toHours(timeLeft) == 1 && TimeUnit.MILLISECONDS.toMinutes(timeLeft) == 0){
            String timeString = (String.format(FORMAT1,
                    Long.toString(TimeUnit.MILLISECONDS.toHours(timeLeft)) + " hour left"));
            notification.setContentText(timeString);
        }
        else if(TimeUnit.MILLISECONDS.toHours(timeLeft) == 1 && TimeUnit.MILLISECONDS.toMinutes(timeLeft) != 0){
            String timeString = (String.format(FORMAT1,
                    Long.toString(TimeUnit.MILLISECONDS.toHours(timeLeft)) + " hour",
                    Long.toString(TimeUnit.MILLISECONDS.toMinutes(timeLeft) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(timeLeft))) + " minutes left"));
            notification.setContentText(timeString);
        }
        else if(TimeUnit.MILLISECONDS.toMinutes(timeLeft) < 60 && TimeUnit.MILLISECONDS.toMinutes(timeLeft) != 0) {
            String timeString = (String.format(FORMAT2,
                    TimeUnit.MILLISECONDS.toMinutes(timeLeft) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(timeLeft)) + " minutes left" ));
            notification.setContentText(timeString);
        }
        else if(TimeUnit.MILLISECONDS.toSeconds(timeLeft) < 60) {
            String timeString = ("Less than 1 minute left");
            notification.setContentText(timeString);
        }
        else if(TimeUnit.MILLISECONDS.toHours(timeLeft) == 0 && TimeUnit.MILLISECONDS.toMinutes(timeLeft) == 0 &&
                TimeUnit.MILLISECONDS.toSeconds(timeLeft) == 0) {
            notification.setContentText("Time Completed!");
        }


        //allows user to click on notification, restart app
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        notification.setContentIntent(pendingIntent);

        //builds notification and issues it
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toggleAirplaneMode() {

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setTitle("Hey!");
        db.setMessage("We recommend turning on Airplane mode.\n"
                + "Please enable Airplane mode.\n");

        db.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        db.setNegativeButton("Close app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
                .show();
    }

    public void testQuestion(){
        final EditText input = new EditText(this);
        input.setHint("Answer Here");
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input);
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setView(layout);
        db.setTitle("Answer This Question!");
        db.setMessage("To stop the timer early, you must answer this question:\n " +
                "What is 2+2?");

        db.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if (!value.equals("4")) {

                } else {
                    countDownTimer.cancel();
                    if (siren.isPlaying())
                        siren.pause();
                    turnOffFlash();
                    timerHasStarted = false;
                    editTime1.setVisibility(View.VISIBLE);
                    timerText.setVisibility(View.INVISIBLE);
                    startButton.setText("Start");
                }

            }
        });
        db.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
                .show();

    }



}




