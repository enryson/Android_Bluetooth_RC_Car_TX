package com.example.enrys.maximusbluetooth;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;

import android.content.BroadcastReceiver;
import android.content.Context;
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

import static android.R.attr.value;

public class MainActivity extends AppCompatActivity {
    Button btnConect, buttonU,buttonD,buttonL,buttonR;
    ProgressBar bt_percentage;

    SeekBar  SeekBar1,SeekBar2;
    TextView textkmh;
    ConnectedThread connectedThread;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mBluetoothDevice = null;
    BluetoothSocket mBluetoothSocket = null;
    int center = 60;

    boolean conection = false;
    public static int oldvalue;
    private static final String TAG = "-->";
    private static final int BT_ACTIVATE_REQUEST = 1;
    private static final int BT_CONNECT_REQUEST = 2;
    private static final int MESSAGE_READ = 3;
    private boolean registered=false;
    StringBuilder bluetoothdata = new StringBuilder();
    private static String MAC = null;
    private Handler mHandler;




    UUID My_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bt_percentage = (ProgressBar) findViewById(R.id.bt_percentage);

        btnConect = (Button) findViewById(R.id.btnConect);


        SeekBar1 = (SeekBar) findViewById(R.id.SeekBar1);
        SeekBar1.setMax(TransportMediator.KEYCODE_MEDIA_RECORD);
        SeekBar1.setProgress(center);

        SeekBar2 = (SeekBar) findViewById(R.id.SeekBar2);
        SeekBar2.setMax(TransportMediator.KEYCODE_MEDIA_RECORD);
        SeekBar2.setProgress(center);


        textkmh = (TextView) findViewById(R.id.textkmh);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "dispositivo bluetooth nao encontrado", Toast.LENGTH_LONG).show();

        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BT_ACTIVATE_REQUEST);
        }

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
                            bt_percentage.setProgress(percentage);
                            textkmh.setText(percentage + "%");
                            //textkmh.setText(finalData+"v");
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


        btnConect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator v2 = (Vibrator) getSystemService(MainActivity.VIBRATOR_SERVICE);
                v2.vibrate(60);
                if (conection) {
                    //Disconect
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
                    //Conect
                    Intent open_list = new Intent(MainActivity.this, DeviceList.class);
                    startActivityForResult(open_list, BT_CONNECT_REQUEST);
                }

            }
        });
        SeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                Vibrator v = (Vibrator)getSystemService(MainActivity.VIBRATOR_SERVICE);
                v.vibrate((progress/10)-3);
                if (conection) {
                    try {
                        connectedThread.write(new StringBuilder(String.valueOf((progress * 1) + 60)).append("n").toString());
                    }catch (Exception e) {
                        connectedThread.write(new StringBuilder(String.valueOf((progress * 1) + 60)).append("n").toString());
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(center);
                if (conection) {
                    try {
                        connectedThread.write(new StringBuilder(String.valueOf((110))).append("n").toString());
                    }catch (Exception e) {
                    }
                }

            }
        });

        SeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                Vibrator v = (Vibrator)getSystemService(MainActivity.VIBRATOR_SERVICE);
                v.vibrate((progress/10)-3);
                if (conection) {
                    try {
                        connectedThread.write(new StringBuilder(String.valueOf((progress + 1)+20)).append("r").toString());
                    }catch (Exception e) {
                        connectedThread.write(new StringBuilder(String.valueOf((progress + 1)+20)).append("r").toString());
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(70);
                if (conection) {
                    try {
                        connectedThread.write(new StringBuilder(String.valueOf((90))).append("r").toString());
                    }catch (Exception e) {
                    }
                }

            }
        });

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
                    if (resultCode == Activity.RESULT_OK){
                        MAC = data.getExtras().getString(DeviceList.MAC_ADRESS);
                        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(MAC);
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

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()


            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    String btdata = new String(buffer, 0 , bytes);

                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, btdata).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String outputwrite ) {
            byte[] msgBuffer = outputwrite.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }*/
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
