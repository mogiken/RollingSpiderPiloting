package com.gashfara.rollingspider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.sql.Date;

public class PilotingActivity extends Activity implements DeviceControllerListener,GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener
{
    private static String TAG = PilotingActivity.class.getSimpleName();
    public static String EXTRA_DEVICE_SERVICE = "pilotingActivity.extra.device.service";

    public DeviceController deviceController;
    public ARDiscoveryDeviceService service;

    private Button emergencyBt;
    private Button takeoffBt;
    private Button landingBt;

    private Button gazUpBt;
    private Button gazDownBt;
    private Button yawLeftBt;
    private Button yawRightBt;

    private Button forwardBt;
    private Button backBt;
    private Button rollLeftBt;
    private Button rollRightBt;

    private TextView batteryLabel;

    private AlertDialog alertDialog;

    ////////////////
    private final String[] SEND_MESSAGES = {"/Action/NONE",  "/Action/UP", "/Action/DOWN", "/Action/RIGHT", "/Action/LEFT"};
    private GoogleApiClient mGoogleApiClient;
    ////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piloting);

        emergencyBt = (Button) findViewById(R.id.emergencyBt);
        emergencyBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (deviceController != null)
                {
                    deviceController.sendEmergency();
                }
            }
        });

        takeoffBt = (Button) findViewById(R.id.takeoffBt);
        takeoffBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (deviceController != null)
                {
                    deviceController.sendTakeoff();
                }
            }
        });
        landingBt = (Button) findViewById(R.id.landingBt);
        landingBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (deviceController != null)
                {
                    deviceController.sendLanding();
                }
            }
        });

        gazUpBt = (Button) findViewById(R.id.gazUpBt);
        gazUpBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.setGaz((byte) 50);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.setGaz((byte)0);

                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        gazDownBt = (Button) findViewById(R.id.gazDownBt);
        gazDownBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.setGaz((byte)-50);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.setGaz((byte)0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        yawLeftBt = (Button) findViewById(R.id.yawLeftBt);
        yawLeftBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.setYaw((byte)-50);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.setYaw((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        yawRightBt = (Button) findViewById(R.id.yawRightBt);
        yawRightBt.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.setYaw((byte) 50);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.setYaw((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        forwardBt = (Button) findViewById(R.id.forwardBt);
        forwardBt.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.setPitch((byte) 50);
                            deviceController.setFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.setPitch((byte) 0);
                            deviceController.setFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        backBt = (Button) findViewById(R.id.backBt);
        backBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.setPitch((byte)-50);
                            deviceController.setFlag((byte)1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.setPitch((byte)0);
                            deviceController.setFlag((byte)0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        rollLeftBt = (Button) findViewById(R.id.rollLeftBt);
        rollLeftBt.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.setRoll((byte) -50);
                            deviceController.setFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.setRoll((byte) 0);
                            deviceController.setFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        rollRightBt = (Button) findViewById(R.id.rollRightBt);
        rollRightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.setRoll((byte)50);
                            deviceController.setFlag((byte)1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.setRoll((byte)0);
                            deviceController.setFlag((byte)0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        batteryLabel = (TextView) findViewById(R.id.batteryLabel);

        Intent intent = getIntent();
        service = intent.getParcelableExtra(EXTRA_DEVICE_SERVICE);

        deviceController = new DeviceController(this, service);
        deviceController.setListener(this);

        /////////////////////
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult.toString());
                    }
                })
                .addApi(Wearable.API)
                .build();

        /////////////////////

    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (deviceController != null)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Connecting ...");

            // create alert dialog
            alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    boolean failed = false;

                    failed = deviceController.start();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            //alertDialog.hide();
                            alertDialog.dismiss();
                        }
                    });

                    if (failed)
                    {
                        finish();
                    }
                    else
                    {
                        //only with RollingSpider in version 1.97 : date and time must be sent to permit a reconnection
                        Date currentDate = new Date(System.currentTimeMillis());
                        deviceController.sendDate(currentDate);
                        deviceController.sendTime(currentDate);
                    }
                }
            }).start();

        }
        ///////////////////
        mGoogleApiClient.connect();
        ///////////////////

    }

    private void stopDeviceController()
    {
        if (deviceController != null)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Disconnecting ...");

            // create alert dialog
            alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    deviceController.stop();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            //alertDialog.hide();
                            alertDialog.dismiss();
                            finish();
                        }
                    });

                }
            }).start();
        }
    }

    @Override
    protected void onStop()
    {
        stopDeviceController();

        super.onStop();
        ///////////////////
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        ///////////////////
    }

    @Override
    public void onBackPressed()
    {
        stopDeviceController();
    }

    @Override
    public void onDisconnect()
    {
        stopDeviceController();
    }

    @Override
    public void onUpdateBattery(final byte percent)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryLabel.setText(String.format("%d%%", percent));
            }
        });

    }
    ////////////////////////////////////////////
    /*
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
    */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived : " + messageEvent.getPath());

        String msg = messageEvent.getPath();
        if (SEND_MESSAGES[1].equals(msg)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "UP", Toast.LENGTH_LONG).show();
                }
            });
            //離陸
            if (deviceController != null) {
                deviceController.sendTakeoff();
                Log.d(TAG, "now:take off");
            }
        } else if (SEND_MESSAGES[2].equals(msg)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "DOWN", Toast.LENGTH_LONG).show();
                }
            });
            //着地
            if (deviceController != null){
                deviceController.sendLanding();
                Log.d(TAG,"now:land");
            }

        } else if (SEND_MESSAGES[3].equals(msg)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "RIGHT", Toast.LENGTH_LONG).show();
                }

            });
            if (deviceController != null) {
                deviceController.setRoll((byte)50);
                deviceController.setFlag((byte) 1);
                Log.d(TAG, "now:right");
                sleep(500);
                deviceController.setRoll((byte) 0);
                deviceController.setFlag((byte) 0);
            }
        } else if (SEND_MESSAGES[4].equals(msg)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "LEFT", Toast.LENGTH_LONG).show();
                }
            });
            if (deviceController != null) {
                deviceController.setRoll((byte)-50);
                deviceController.setFlag((byte) 1);
                Log.d(TAG, "now:left");
                sleep(500);
                deviceController.setRoll((byte)0);
                deviceController.setFlag((byte)0);

            }
        }
    }
    ////////////////////////////////////////////
    public synchronized void sleep(long msec)
    {
        try
        {
            wait(msec);
        }catch(InterruptedException e){}
    }

}
