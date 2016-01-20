package com.gashfara.rollingspider;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener{
    private final String TAG = MainActivity.class.getName();
    private final String[] SEND_MESSAGES = {"/Action/NONE",  "/Action/UP", "/Action/DOWN", "/Action/RIGHT", "/Action/LEFT"};

    private TextView mTextView;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private String mNode;

    //////
    //THRESHOLD ある値以上を検出するための閾値
    protected final static double THRESHOLD=2.0;//12 4.0
    protected final static double THRESHOLD_MIN=1;

    //low pass filter alpha ローパスフィルタのアルファ値
    protected final static double alpha= 0.8;

    //端末が実際に取得した加速度値。重力加速度も含まれる。This values include gravity force.
    private float[] currentOrientationValues = { 0.0f, 0.0f, 0.0f };
    //ローパス、ハイパスフィルタ後の加速度値 Values after low pass and high pass filter
    private float[] currentAccelerationValues = { 0.0f, 0.0f, 0.0f };

    //diff 差分
    private float dx=0.0f;
    private float dy=0.0f;
    private float dz=0.0f;

    //previous data 1つ前の値
    private float old_x=0.0f;
    private float old_y=0.0f;
    private float old_z=0.0f;

    //ベクトル量
    private double vectorSize=0;

    //カウンタ
    long counter=0;

    //一回目のゆれを省くカウントフラグ（一回の端末の揺れで2回データが取れてしまうのを防ぐため）
    //count flag to prevent aquiring data twice with one movement of a device
    boolean counted=true;

    // X軸加速方向
    boolean vecx = true;
    // Y軸加速方向
    boolean vecy = true;
    // Z軸加速方向
    boolean vecz = true;


    //ノイズ対策
    boolean noiseflg=true;
    //ベクトル量(最大値)
    private double vectorSize_max=0;


    public float getx(){return this.dx;}
    public float gety(){return this.dy;}
    public float getz(){return this.dz;}
    public long getcounter(){return this.counter;}
    public double getVectorMax(){return this.vectorSize_max;}

    SensorManager manager;
    ////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        ////////
        //mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        // construct sensor
        List<Sensor> sensors =manager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if(sensors.size()>0){
            Sensor s =sensors.get(0);
            //manager.registerListener(this, s,SensorManager.SENSOR_DELAY_UI);
            manager.registerListener(this, s,SensorManager.SENSOR_DELAY_FASTEST);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected");

//                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                //Nodeは１個に限定
                                if (nodes.getNodes().size() > 0) {
                                    mNode = nodes.getNodes().get(0).getId();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended");

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed : " + connectionResult.toString());
                    }
                })
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //////////////
        //Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);


        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ////////////
        //mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // 取得 Acquiring data

            // ローパスフィルタで重力値を抽出　Isolate the force of gravity with the low-pass filter.
            currentOrientationValues[0] = event.values[0] * 0.1f + currentOrientationValues[0] * (1.0f - 0.1f);
            currentOrientationValues[1] = event.values[1] * 0.1f + currentOrientationValues[1] * (1.0f - 0.1f);
            currentOrientationValues[2] = event.values[2] * 0.1f + currentOrientationValues[2] * (1.0f - 0.1f);

            // 重力の値を省くRemove the gravity contribution with the high-pass filter.
            currentAccelerationValues[0] = event.values[0] - currentOrientationValues[0];
            currentAccelerationValues[1] = event.values[1] - currentOrientationValues[1];
            currentAccelerationValues[2] = event.values[2] - currentOrientationValues[2];

            // ベクトル値を求めるために差分を計算　diff for vector
            dx = currentAccelerationValues[0] - old_x;
            dy = currentAccelerationValues[1] - old_y;
            dz = currentAccelerationValues[2] - old_z;

            vectorSize = Math.sqrt((double) (dx * dx + dy * dy + dz * dz));

            // 一回目はノイズになるから省く
            if (noiseflg == true) {
                noiseflg = false;
            } else {

                if (vectorSize > THRESHOLD /* && dz <0.0f */) {
                    if (counted == true) {
                        System.out.println(dx + "," + dz + "," + vectorSize);
                        int motion;
                        motion = detectMotion(dx, dy, dz);

                        if (motion > 0 && mNode != null ) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_MESSAGES[motion], null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult result) {
                                    if (!result.getStatus().isSuccess()) {
                                        Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
                                    }
                                    Log.d(TAG,"send ok");
                                }
                            });
                        }

                        counter++;
                        counted = false;
                        // System.out.println("count is "+counter);
                        // 最大値なら格納
                        if (vectorSize > vectorSize_max) {
                            vectorSize_max = vectorSize;
                        }
                    } else if(counted== false) {
                        counted = true;

                    }

                }
            }

            // 状態更新
            //vectorSize_old = vectorSize;
            old_x = currentAccelerationValues[0];
            old_y = currentAccelerationValues[1];
            old_z = currentAccelerationValues[2];

        }

    }

    /**
     * 超適当な判定
     *
     */
    //前のデータ
    float ox=0, oy=0,  oz=0;
    private int detectMotion(float x, float y, float z) {
        int motion = 0;
        Log.d(TAG, "s:"  + (int)x + "/" + (int)y + "/" + (int)z);



        if (y > 1 && oy > 1) {//10
            Log.d(TAG, "RIGHT!"+y);
            motion = 3;
        } else if (y < -1 && oy < -1) {//10
            Log.d(TAG, "LEFT!"+y);
            motion = 4;
        }else if (z > 1 && oz > 1) {//5
            Log.d(TAG, "up!"+z);
            motion = 1;
        } else if (z < -1 && oz < -1) {//5
            Log.d(TAG, "down!"+z);
            motion = 2;
        }
        //前のデータ
        ox = x;
        oy = y;
        oz = z;
        if (mTextView != null) mTextView.setText(SEND_MESSAGES[motion]);
        return motion;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
