package com.example.ahsan.masumvaiproject;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class WheelControlActivity extends AppCompatActivity {


    private final String DEVICE_ADDRESS="98:D3:31:FB:7A:9A";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Serial Port Service ID
    private BluetoothDevice device;
    BluetoothSocket socket = null;
    BluetoothAdapter myBluetooth = null;
    private OutputStream outputStream;
    private InputStream inputStream;

    private Button startButton, map,forward, backword, left, right, voiceButton, stop, fastB, mediumB, slowB;
    TextView textView5,textView6,textView7;
    boolean deviceConnected=false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    final String myKey = "ambulance";
    final String forwardKey = "go";
    final String backwordKey = "back";
    final String rightKey = "right";
    final String leftKey = "left";
    final String stopKey = "stop";
    private String helpingHandContactNo;
    private Boolean isBtConnected = false;

    private DatabaseReference mdatabaseUsers;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;
    private DatabaseReference mHelpingHandDatabase;

    boolean clickedF=false;
    boolean clickedS=false;
    boolean clickedM=false;

    private Button logOut;

    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;

    private String code = "help";

    private Thread repeatTaskThread;
    ArrayList<String> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel_control);

        final PatientsMapsActivity ww = new PatientsMapsActivity();

        mdatabaseUsers = FirebaseDatabase.getInstance().getReference().child("PatientsData");
        mAuth = FirebaseAuth.getInstance();

        startButton = (Button) findViewById(R.id.buttonStart);
        forward = findViewById(R.id.buttonF);
        backword = findViewById(R.id.buttonB);
        left = findViewById(R.id.buttonL);
        right = findViewById(R.id.buttonR);
        voiceButton = (Button) findViewById(R.id.buttonVoice);
        map = findViewById(R.id.patientMapButton);
        logOut = findViewById(R.id.buttonLogout);
        stop = findViewById(R.id.buttonStop);

        fastB = findViewById(R.id.fast);
        mediumB = findViewById(R.id.medium);
        slowB = findViewById(R.id.slow);

        pressed();

        setUiEnabled(false);



        current_user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_user.getUid());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                mHelpingHandDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_user.getUid());

                mHelpingHandDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        helpingHandContactNo  = dataSnapshot.child("contact_no").getValue().toString();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



//        logOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                PatientsMapsActivity.fa.finish();
//                ww.destroy();
//
//                FirebaseAuth.getInstance().signOut();
//                Intent intent = new Intent(WheelControlActivity.this,MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
////                finish();
////                return;
//            }
//        });

        logOut.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        map.getBackground().setAlpha(100);
                        PatientsMapsActivity.fa.finish();
                        ww.destroy();

                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(WheelControlActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
//                finish();
//                return;
                        break;
                    case MotionEvent.ACTION_UP:
                        map.getBackground().setAlpha(255);
                }
                return false;
            }
        });





        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        map.getBackground().setAlpha(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        map.getBackground().setAlpha(255);
                        Intent intent = new Intent(WheelControlActivity.this,PatientsMapsActivity.class);
                        startActivity(intent);
                }
                return false;
            }
        });







        //voice Part

        checkPermission();

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

                matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null) {

                    Toast.makeText(WheelControlActivity.this,matches.get(0).toString(),Toast.LENGTH_SHORT).show();

                    if (matches.get(0).toString().toLowerCase().equals(myKey)) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        autoApproach();


                    } else if (matches.get(0).toString().toLowerCase().indexOf(myKey) != -1) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        autoApproach();


                    } else if (matches.get(0).toString().toLowerCase().equals(forwardKey)) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        goForward();


                    } else if (matches.get(0).toString().toLowerCase().indexOf(forwardKey) != -1) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        goForward();


                    } else if (matches.get(0).toString().toLowerCase().equals(backwordKey)) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        goBackword();


                    } else if (matches.get(0).toString().toLowerCase().indexOf(backwordKey) != -1) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        goBackword();


                    } else if (matches.get(0).toString().toLowerCase().equals(rightKey)) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        goRight();


                    } else if (matches.get(0).toString().toLowerCase().indexOf(rightKey) != -1) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        goRight();


                    } else if (matches.get(0).toString().toLowerCase().equals(leftKey)) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        goLeft();


                    } else if (matches.get(0).toString().toLowerCase().indexOf(leftKey) != -1) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        goLeft();


                    } else if (matches.get(0).toString().toLowerCase().equals(stopKey)) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        stopStop();


                    } else if (matches.get(0).toString().toLowerCase().indexOf(stopKey) != -1) {
                        //Toast.makeText(WheelControlActivity.this, "Um in Trouble!", Toast.LENGTH_LONG).show();

                        stopStop();


                    }

                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });



        fastB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        fastB.getBackground().setAlpha(100);
                        mediumB.getBackground().setAlpha(255);
                        slowB.getBackground().setAlpha(255);
                        fastB.setPressed(true);
                        slowB.setPressed(false);
                        mediumB.setPressed(false);
                        clickedF = true;
                        clickedM = false;
                        clickedS = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        fastB.getBackground().setAlpha(100);
                        //mediumB.getBackground().setAlpha(255);
                        //slowB.getBackground().setAlpha(255);

                }
                return false;
            }
        });


        mediumB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mediumB.getBackground().setAlpha(100);
                        fastB.getBackground().setAlpha(255);
                        slowB.getBackground().setAlpha(255);
                        fastB.setPressed(false);
                        slowB.setPressed(false);
                        mediumB.setPressed(true);
                        clickedF = false;
                        clickedM = true;
                        clickedS = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        mediumB.getBackground().setAlpha(100);

                }
                return false;
            }
        });


        slowB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        slowB.getBackground().setAlpha(100);
                        mediumB.getBackground().setAlpha(255);
                        fastB.getBackground().setAlpha(255);
                        fastB.setPressed(false);
                        slowB.setPressed(true);
                        mediumB.setPressed(false);
                        clickedF = false;
                        clickedM = false;
                        clickedS = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        slowB.getBackground().setAlpha(100);


                }
                return false;
            }
        });










        voiceButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();

                        break;
                    case MotionEvent.ACTION_DOWN:
                        voiceButton.setBackgroundColor(Color.GREEN);
                        voiceButton.setText("On my job!");

                        RepeatTask();
                        break;
                }

                return false;
            }
        });

        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        if (clickedF){
                            goForward();
                        }else if (clickedM){
                            goForwardm();
                        }else {
                            goForwards();
                        }

                        forward.getBackground().setAlpha(130);
                        break;
                    case MotionEvent.ACTION_UP:
                        stopStop();
                        forward.getBackground().setAlpha(255);

                }
                return false;
            }
        });




        backword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:


                        if (clickedF){
                            goBackword();
                        }else if (clickedM){
                            goBackwordm();
                        }else {
                            goBackwords();
                        }

                        backword.getBackground().setAlpha(130);
                        break;
                    case MotionEvent.ACTION_UP:
                        stopStop();
                        backword.getBackground().setAlpha(255);

                }
                return false;
            }
        });




        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        if (clickedF){
                            goLeft();
                        }else if (clickedM){
                            goLeftm();
                        }else {
                            goLefts();
                        }


                        left.getBackground().setAlpha(130);
                        break;
                    case MotionEvent.ACTION_UP:
                        stopStop();
                        left.getBackground().setAlpha(255);

                }
                return false;
            }
        });




        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:


                        if (clickedF){
                            goRight();
                        }else if (clickedM){
                            goRightm();
                        }else {
                            goRights();
                        }


                        right.getBackground().setAlpha(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        stopStop();
                        right.getBackground().setAlpha(255);

                }
                return false;
            }
        });


        stop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        Disconnect();


                        stop.getBackground().setAlpha(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        stop.getBackground().setAlpha(255);

                }
                return false;
            }
        });
    }

    private void pressed() {
        fastB.setPressed(false);
        slowB.setPressed(false);
        mediumB.setPressed(true);
        mediumB.getBackground().setAlpha(100);
        slowB.getBackground().setAlpha(255);
        fastB.getBackground().setAlpha(255);
    }


    private void autoApproach() {

//        String[] splitData = data.split("\\r\\n");
//        String value = splitData[0];
//
//
//        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//
//        String userId = mAuth.getCurrentUser().getUid();
//
//        String[] bpmAmpValue = value.split("-");
//
//        String bpmValue = bpmAmpValue[0];
//        String ampValue = bpmAmpValue[1];
//
//        mdatabaseUsers.child(userId).child("Time").setValue(mydate);
//        mdatabaseUsers.child(userId).child("BPM").setValue(bpmValue);
//        mdatabaseUsers.child(userId).child("AMP").setValue(ampValue);




        Intent callIntent = new Intent(Intent.ACTION_CALL);                                                //------********-----//
        callIntent.setData(Uri.parse(helpingHandContactNo));   //"tel:+8801778619115"
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            // int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(callIntent);
        stopThread = true;

        //Toast.makeText(WheelControlActivity.this,"AMBULANCE COME COME...",Toast.LENGTH_LONG).show();



    }






    //voice

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+getPackageName()));
                startActivity(intent);
                finish();

            }
        }
    }







    private void RepeatTask()
    {
        repeatTaskThread = new Thread()
        {
            public void run()
            {
                while (true)
                {

//                    FetchURL fu = new FetchURL();
//                    fu.Run("http://192.168.0.10/joins.txt");
//                    String o = fu.getOutput();
                    // Update TextView in runOnUiThread
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                            //txtJoins.setText("Last connections: \n" + o);
                        }
                    });
                    try
                    {
                        // Sleep for
                        Thread.sleep(5000);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        repeatTaskThread.start();
    }




    private void goForward() {

            if (socket != null)
            {
                try {
                    socket.getOutputStream().write("A".toString().getBytes());
                } catch (IOException e) {
                    Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
            }
    }


    private void goForwardm() {

        if (socket != null)
        {
            try {
                socket.getOutputStream().write("B".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void goForwards() {

        if (socket != null)
        {
            try {
                socket.getOutputStream().write("C".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }


    private void goBackword() {

        if (socket != null)
        {
            try {
                socket.getOutputStream().write("D".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void goBackwordm() {

        if (socket != null)
        {
            try {
                socket.getOutputStream().write("E".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void goBackwords() {

        if (socket != null)
        {
            try {
                socket.getOutputStream().write("F".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void goLeft() {

        if (socket != null)
        {
            try {
                socket.getOutputStream().write("G".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void goLeftm() {

        if (socket != null)
        {
            try {
                socket.getOutputStream().write("H".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void goLefts() {

        if (socket != null)
        {
            try {
                socket.getOutputStream().write("I".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }


    private void goRight() {


        if (socket != null)
        {
            try {

                socket.getOutputStream().write("J".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }


    private void goRightm() {


        if (socket != null)
        {
            try {

                socket.getOutputStream().write("K".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }


    private void goRights() {


        if (socket != null)
        {
            try {

                socket.getOutputStream().write("L".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }






    private void stopStop() {


        if (socket != null)
        {
            try {

                socket.getOutputStream().write("S".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(WheelControlActivity.this,"ERROR "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WheelControlActivity.this,"No Bluetooth connection",Toast.LENGTH_SHORT).show();
        }
    }







    public void onClickStart(View view) {
        if(BTinit())
        {
            new ConnectBT().execute();
//            if(BTconnect())
//            {
//                deviceConnected=true;
//                setUiEnabled(true);
//                startButton.setBackgroundColor(Color.GREEN);
//                startButton.setText("CONNECTED");
//
            } else {
                Toast.makeText(WheelControlActivity.this,"Can't connect! check connection...",Toast.LENGTH_SHORT).show();
            }
        }

    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }



    public void setUiEnabled(boolean bool)
    {
        startButton.setEnabled(!bool);

    }




    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return connected;
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private Boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress = ProgressDialog.show(LedControl.this,"Connecting...","Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (socket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(DEVICE_ADDRESS);
                    socket = dispositivo.createInsecureRfcommSocketToServiceRecord(PORT_UUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    socket.connect();
                }
            }catch (IOException e) {
                connectSuccess = false;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!connectSuccess)
            {
                Toast.makeText(WheelControlActivity.this,"Connection Failed!!! Try Again....",Toast.LENGTH_SHORT).show();
//                msg("Connection Failed!!! Try Again....");
                finish();
            }else {
                //msg("Connected");
                Toast.makeText(WheelControlActivity.this,"Connected",Toast.LENGTH_SHORT).show();
                deviceConnected=true;
                setUiEnabled(true);
                startButton.setBackgroundColor(Color.GREEN);
                startButton.setText("CONNECTED");
                isBtConnected = true;
            }
            //progress.dismiss();
        }

    }

    private void Disconnect() {
        if (socket!=null)
        {
            try
            {
                socket.close();
                Toast.makeText(WheelControlActivity.this,"Bluetooth disconnected!",Toast.LENGTH_SHORT).show();
            }
            catch (IOException e)
            {
               // msg("Error!");
                Toast.makeText(WheelControlActivity.this,"Error!",Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (isBtConnected){
            startButton.setBackgroundColor(Color.GREEN);
            startButton.setText("CONNECTED");
            startButton.setTextColor(Color.WHITE);
        } else {
            setUiEnabled(false);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Disconnect();
    }

}
