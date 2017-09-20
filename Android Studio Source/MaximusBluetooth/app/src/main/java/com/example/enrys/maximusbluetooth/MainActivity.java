package com.example.enrys.maximusbluetooth;

import android.annotation.TargetApi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.TransportMediator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.R.attr.progress;
import static android.R.attr.value;

public class MainActivity extends AppCompatActivity {
    Button btnConect;
    SeekBar  SeekBarD,SeekBarA,SeekBarCalibrate;
    TextView textkmh,textViewCenterValue,textViewBTPercentage;
    ConnectedThread connectedThread;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mBluetoothDevice = null;
    BluetoothSocket mBluetoothSocket = null;

    int center = 60;
    int centerCalibrate = center;
    int seekBarAMin ;

    public ProgressBar progressBar;

    boolean conection = false;
    public static int oldvalue;
    private static final String TAG = "-->";
    private static final int BT_ACTIVATE_REQUEST = 1;
    private static final int BT_CONNECT_REQUEST = 2;
    private static final int MESSAGE_READ = 3;
    private static final int GET_MAC_DEVICE = 4;

    //private static final int FinalMac = 4;

    private boolean registered=false;
    StringBuilder bluetoothdata = new StringBuilder();
    private static String MAC = null;
    public String MACD;
    private Handler mHandler;


    UUID My_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    private void SendServo(final int progesso)
    {
        //Velocimetro.setText(v);
        mHandler.post(new Runnable() {
            public void run(){
                String v = String.valueOf((progesso)+40);
                try
                {
                    connectedThread.write(new StringBuilder(v).append("n").toString());
                    oldvalue = progesso+1;
                } catch (Exception e){}
            }
        });
    }
    private void SendESC(final int progesso)
    {
        //Velocimetro.setText(v);
        mHandler.post(new Runnable() {
            public void run(){
                String v = String.valueOf((progesso)+40);
                try
                {
                    connectedThread.write(new StringBuilder(v).append("r").toString());
                    oldvalue = progesso+1;
                } catch (Exception e){}
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConect = (Button) findViewById(R.id.btnConect);                              //Set Itens Screen
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //////////////////////////////////////
        SeekBarD = (SeekBar) findViewById(R.id.SeekBarD);
        SeekBarD.setMax(TransportMediator.KEYCODE_MEDIA_RECORD);
        SeekBarD.setProgress(center);
        //////////////////////////////////////
        SeekBarA = (SeekBar) findViewById(R.id.SeekBarA);
        SeekBarA.setMax(TransportMediator.KEYCODE_MEDIA_RECORD);
        SeekBarA.setProgress(center);
        //////////////////////////////////////
        SeekBarCalibrate = (SeekBar) findViewById(R.id.seekBarCalibrate);
        //////////////////////////////////////
        textkmh = (TextView) findViewById(R.id.textkmh);
        textViewBTPercentage = (TextView) findViewById(R.id.textViewBTPercentage);
        textViewCenterValue = (TextView) findViewById(R.id.textViewCenterValue);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "dispositivo bluetooth nao encontrado", Toast.LENGTH_LONG).show();

        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BT_ACTIVATE_REQUEST);
        }

        btnConect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator v2 = (Vibrator) getSystemService(MainActivity.VIBRATOR_SERVICE);
                v2.vibrate(60);

                if (conection) {
                    try {
                        mBluetoothSocket.close();
                        conection = true;
                        Toast.makeText(getApplicationContext(), "Device Desconectado : ", Toast.LENGTH_LONG).show();
                        //mudando nome do botao conecao
                        btnConect.setText("conectar");

                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Erro Desconectado : " + erro, Toast.LENGTH_LONG).show();
                    }

                } else {
                    Intent open_list = new Intent(MainActivity.this, DeviceList.class);
                    startActivityForResult(open_list, BT_CONNECT_REQUEST);


                    /*Intent getmac = new Intent(MainActivity.this, MainActivity.class);
                    SharedPreferences MAC_lido = getSharedPreferences("Saved_MAC", MODE_PRIVATE);
                    String result = MAC_lido.getString("MAC", null);
                    if (MAC_lido == null) {
                        textkmh.setText("NaoLido");
                        startActivityForResult(open_list, BT_CONNECT_REQUEST);
                    } else {
                        MACD = result;
                        startActivityForResult(getmac,GET_MAC_DEVICE);
                    }*/
                }

            }
        });

        SeekBarCalibrate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                centerCalibrate = progress;
                seekBar.getProgress();
                try {
                    String valuecenter = String.valueOf(centerCalibrate);
                    textViewCenterValue.setText(valuecenter);
                }
                catch (Exception e)
                {

                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {            }
        });

        SeekBarD.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                Vibrator v = (Vibrator)getSystemService(MainActivity.VIBRATOR_SERVICE);
                v.vibrate((progress/10)-3);
                SendServo(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(centerCalibrate);
            }
        });

        SeekBarA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                Vibrator v = (Vibrator)getSystemService(MainActivity.VIBRATOR_SERVICE);
                v.vibrate((progress/10)-3);
                SendESC(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(center-5);
            }
        });

        mHandler =  new Handler() {
            @Override
            public void handleMessage(Message msg) {                                    //Read data from the handler to recilve voltage
                if (msg.what == MESSAGE_READ){
                    String recilvdata = (String) msg.obj;
                    bluetoothdata.append(recilvdata);
                    int endinformation = bluetoothdata.indexOf("v");
                    if (endinformation >0){
                        String completeData = bluetoothdata.substring(00,endinformation);
                        int informationLeght = completeData.length();
                        if(bluetoothdata.charAt(0)=='{'){
                            String finalData = bluetoothdata.substring(1,informationLeght);
                            double minVolt = 10;
                            double maxVolt = 12.2;
                            double volt = (Double.parseDouble(finalData)-minVolt);
                            int percentage = (int) ((volt*100)/(maxVolt-minVolt));
                            progressBar.setProgress(percentage);
                            textViewBTPercentage.setText(percentage + "%");
                            textkmh.setText("Voltage : "+ finalData);
                        }
                    }
                    bluetoothdata.delete(0, bluetoothdata.length());

                }
            }
        };

        final Handler handler = new Handler();                                          //Send String to the handler m
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            connectedThread.write("m");
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 400);

    }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode){
                case BT_ACTIVATE_REQUEST:
                    if(resultCode == Activity.RESULT_OK){
                        Toast.makeText(getApplicationContext(), "bluetooth Ativado",Toast.LENGTH_LONG).show();
                    }   else {
                        Toast.makeText(getApplicationContext(), "bluetooth nao ativado",Toast.LENGTH_LONG).show();
                        finish();
                    }
                    break;

                case BT_CONNECT_REQUEST:
                    MAC = data.getExtras().getString(DeviceList.MAC_ADRESS);

                    //SharedPreferences sp = getSharedPreferences("Saved_MAC", MODE_PRIVATE);
                    //SharedPreferences.Editor edit = sp.edit();
                    //edit.putString("MAC",MAC);
                    //edit.apply();
                    //MAC = data.getExtras().getString(MAC);
                    //textkmh.setText(MACD);
                    if (resultCode == Activity.RESULT_OK){

                        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(MAC);
                        //textkmh.setText(MAC);
                        try {
                            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(My_UUID);
                            mBluetoothSocket.connect();
                            conection = true;
                            connectedThread = new ConnectedThread(mBluetoothSocket);
                            connectedThread.start();
                            //mudando nome do botao conecao
                            btnConect.setText("Desconectar");
                            Toast.makeText(getApplicationContext(), "Conectado : "+ MAC, Toast.LENGTH_LONG).show();
                        }catch(IOException erro){
                            conection = false;
                            Toast.makeText(getApplicationContext(), "Erro Desconectado : "+ MAC, Toast.LENGTH_LONG).show();
                        }
                    }   else    {
                        Toast.makeText(getApplicationContext(), "Falha MAC",Toast.LENGTH_LONG).show();
                    }
            }
        }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {                                                                       // Get the input and output streams, using temp objects because
                tmpIn = socket.getInputStream();                                        // member streams are final
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];                                             // buffer store for the stream
            int bytes;                                                                  // bytes returned from read()
            while (true) {                                                              // Keep listening to the InputStream until an exception occurs
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    String btdata = new String(buffer, 0 , bytes);                      // Send the obtained bytes to the UI activity

                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, btdata).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String outputwrite ) {                                         /* Call this from the main activity to send data to the remote device */
            byte[] msgBuffer = outputwrite.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) { }
        }


        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    Intent intent1 = new Intent(MainActivity.this, MainActivity.class);

                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            if(registered) {
                                unregisterReceiver(mReceiver);
                                registered=false;
                            }
                            startActivity(intent1);
                            finish();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if(registered) {
                                unregisterReceiver(mReceiver);
                                registered=false;
                            }
                            startActivity(intent1);
                            finish();
                            break;
                    }
                }
            }
        };
    }

}
