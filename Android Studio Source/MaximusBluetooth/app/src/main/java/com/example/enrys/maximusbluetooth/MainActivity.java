package com.example.enrys.maximusbluetooth;

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
import java.util.UUID;

import static android.R.attr.value;

public class MainActivity extends AppCompatActivity {
    Button btnConect, buttonU,buttonD,buttonL,buttonR;
    SeekBar  SeekBarD,SeekBarA;
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

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConect = (Button) findViewById(R.id.btnConect);

        SeekBarD = (SeekBar) findViewById(R.id.SeekBarD);
        SeekBarD.setMax(TransportMediator.KEYCODE_MEDIA_RECORD);
        SeekBarD.setProgress(center);

        SeekBarA = (SeekBar) findViewById(R.id.SeekBarA);
        SeekBarA.setMax(TransportMediator.KEYCODE_MEDIA_RECORD);
        SeekBarA.setProgress(center);


        textkmh = (TextView) findViewById(R.id.textkmh);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "dispositivo bluetooth nao encontrado", Toast.LENGTH_LONG).show();

        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BT_ACTIVATE_REQUEST);
        }

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {

                if (msg.what == MESSAGE_READ) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    bluetoothdata.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = bluetoothdata.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = bluetoothdata.substring(0, 4);    // extract string


                        double volt2 = Double.parseDouble(dataInPrint);
                        double voltfinal = volt2/100;
                        double numberbase = ((volt2)-1900)*10/36 ;
                        int calc = (int) numberbase;
                        //int voltagem = Integer.valueOf(volt2);

                        textkmh.setText((voltfinal)+"V");
                        progressBar.setProgress(calc);

                        int dataLength = dataInPrint.length();                          //get length of data received
                        //txtStringLength.setText("String Length = " + String.valueOf(dataLength));
                        if (bluetoothdata.charAt(0) == '#')                             //if it starts with # we know it is what we are looking for
                        {
                            String volt = bluetoothdata.substring(1, dataLength);
                            //int voltagem = Integer.valueOf(volt);
                            //progressBar2.setProgress(voltagem*100);
                            //textkmh.setText(volt+"v");
                        }
                        bluetoothdata.delete(0, bluetoothdata.length());                    //clear all string data
                        // readMessage =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };


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
        SeekBarD.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                Vibrator v = (Vibrator)getSystemService(MainActivity.VIBRATOR_SERVICE);
                v.vibrate((progress/10)-3);
                SendServo(progress);
                /*if (conection) {
                    try {
                        connectedThread.write(new StringBuilder(String.valueOf((progress * 1) + 60)).append("n").toString());
                    }catch (Exception e) {
                        connectedThread.write(new StringBuilder(String.valueOf((progress * 1) + 60)).append("n").toString());
                    }
                }*/
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(65);
                /*
                if (conection) {
                    try {
                        connectedThread.write(new StringBuilder(String.valueOf((110))).append("n").toString());
                    }catch (Exception e) {
                    }
                }*/

            }
        });

        SeekBarA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                Vibrator v = (Vibrator)getSystemService(MainActivity.VIBRATOR_SERVICE);
                v.vibrate((progress/10)-3);
                SendESC(progress);
                /*if (conection) {
                    try {
                        connectedThread.write(new StringBuilder(String.valueOf((progress + 1)+20)).append("r").toString());
                    }catch (Exception e) {
                        connectedThread.write(new StringBuilder(String.valueOf((progress + 1)+20)).append("r").toString());
                    }
                }*/
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(55);
                /*if (conection) {
                    try {
                        connectedThread.write(new StringBuilder(String.valueOf((90))).append("r").toString());
                    }catch (Exception e) {
                    }
                }*/

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
