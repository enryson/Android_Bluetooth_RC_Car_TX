package com.example.enrys.maximusbluetooth;

import android.annotation.TargetApi;

import android.graphics.PorterDuff;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.media.TransportMediator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
    SeekBar  SeekBar1,SeekBar2;
    ConnectedThread connectedThread;
    TextView textView;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mBluetoothDevice = null;
    BluetoothSocket mBluetoothSocket = null;
    int center = 60;
    public int valor1,valor2;

    boolean conection = false;
    private Vibrator vibrator;
    public static int oldvalue;
    private static final String TAG = "-->";
    private static final int BT_ACTIVATE_REQUEST = 1;
    private static final int BT_CONNECT_REQUEST = 2;
    private static String MAC = null;
    private Handler mHandler = new Handler();
    private static final String MESSAGE_READ = null;

    UUID My_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConect = (Button) findViewById(R.id.btnConect);

        SeekBar1 = (SeekBar) findViewById(R.id.SeekBar1);
        SeekBar1.setMax(TransportMediator.KEYCODE_MEDIA_RECORD);
        SeekBar1.setProgress(center);
        textView = (TextView) findViewById(R.id.textView);


        SeekBar2 = (SeekBar) findViewById(R.id.SeekBar2);
        SeekBar2.setMax(TransportMediator.KEYCODE_MEDIA_RECORD);
        SeekBar2.setProgress(center);

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
                try {
                    valor2 = (progress + 1)+20;
                    //connectedThread.write(new StringBuilder(String.valueOf((progress * 1) + 60)).append("n").toString());
                }catch (Exception e) {
                    //connectedThread.write(new StringBuilder(String.valueOf((progress * 1) + 60)).append("n").toString());
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(center);
                try {
                    valor2 = (progress + 1)+20;
                    //connectedThread.write(new StringBuilder(String.valueOf((110))).append("n").toString());

                }catch (Exception e ){

                }

            }
        });

        SeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                Vibrator v = (Vibrator)getSystemService(MainActivity.VIBRATOR_SERVICE);
                v.vibrate((progress/10)-3);
                try {
                    valor1 = (progress + 1)+20;
                    //connectedThread.write(new StringBuilder(String.valueOf((progress + 1)+20)).append("r").toString());
                }catch (Exception e) {
                    //connectedThread.write(new StringBuilder(String.valueOf((progress + 1)+20)).append("r").toString());
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(70);
                try {
                    valor1 = (progress + 1)+20;
                }catch (Exception e ){

                }

                //connectedThread.write(new StringBuilder(String.valueOf((90))).append("r").toString());
            }
        });



        final Handler handler2 = new Handler();
        Timer timer2 = new Timer();
        TimerTask doAsynchronousTask2 = new TimerTask() {
            @Override
            public void run() {
                handler2.post(new Runnable() {
                    public void run() {
                        try {
                            textView.setText(valor1);
                            connectedThread.write(new StringBuilder(String.valueOf(valor1)).append("n").toString());
                            //connectedThread.write(new StringBuilder(String.valueOf(value)).append("n").toString());
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer2.schedule(doAsynchronousTask2, 0, 1);

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
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) { }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;
            }
            public void write(String outputwrite ) {
                byte[] msgBuffer = outputwrite.getBytes();
                try {
                    mmOutStream.write(msgBuffer);
                } catch (IOException e) { }
            }

        }

}
