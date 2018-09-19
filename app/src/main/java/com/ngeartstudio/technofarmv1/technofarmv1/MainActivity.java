package com.ngeartstudio.technofarmv1.technofarmv1;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.ntt.customgaugeview.library.GaugeView;

import java.util.Calendar;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public Button setHold,setTime;
    public GraphView graph;
    ToggleButton manual;
    public EditText setKelembaban,time;
    private FirebaseDatabase database;
    private SpeedometerGauge speedometer;
    public TextView statussuhu, statuskelembaban ,statussuhu2,statuskelembaban2;
    float suhu = 0;
    float kelembaban = 0;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseInstance;
    public ProgressBar loading;
    RelativeLayout proses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setHold = (Button) findViewById(R.id.setHold);
        setTime = (Button) findViewById(R.id.setTime);
        setKelembaban = (EditText) findViewById(R.id.hold);
        time = (EditText) findViewById(R.id.time);
        database = FirebaseDatabase.getInstance();
        manual = (ToggleButton) findViewById(R.id.manual);
        statussuhu = (TextView) findViewById(R.id.statussuhu);
        statuskelembaban = (TextView) findViewById(R.id.statuskelembaban);
        statussuhu2 = (TextView) findViewById(R.id.statussuhu2);
        statuskelembaban2 = (TextView) findViewById(R.id.statuskelembaban2);
        speedometer = (SpeedometerGauge) findViewById(R.id.speedometer);
        loading = (ProgressBar) findViewById(R.id.progressBar3);
        proses = (RelativeLayout) findViewById(R.id.layoutproses);

        mFirebaseInstance = FirebaseDatabase.getInstance();

        final DatabaseReference myRef = database.getReference("tomat");
        manual.setOnClickListener(new  View.OnClickListener(){
            public void onClick(View v){
                StringBuilder result = new StringBuilder();
                //result.append("ToggleButton1 : ").append(manual.getText());
                  //myRef.child("Manual").setValue(manual.getText());
                if (manual.getText().equals("On")) {
                    //database.getReference("123").setValue(1);
                    myRef.child("manual").setValue(1);
                } else if (manual.getText().equals("Off")) {
                    //database.getReference("123").setValue(0);
                    myRef.child("manual").setValue(0);
                }
                Toast.makeText(getApplicationContext(), "Penyiraman Manual " + manual.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        setHold.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String lembab = setKelembaban.getText().toString();
                int angkalembab = Integer.parseInt(lembab);
                //TODO something when floating action menu first item clicked
                //database.getReference("set_kelembaban").setValue(angkalembab);
                myRef.child("set_kelembaban").setValue(angkalembab);
                if (setKelembaban.getText().toString().length() == 0)
                    setKelembaban.setError("Harus diisi");

                if (TextUtils.isEmpty(lembab)) {
                    Toast.makeText(getApplicationContext(), "Masukan Pengaturan Kelembaban !", Toast.LENGTH_SHORT).show();
                    requestFocus(setKelembaban
                    );
                    return;
                } else {
                    Toast.makeText(getApplicationContext(), "Pengaturan Penyiraman Untuk Kelembaban " + lembab + " Berhasil", Toast.LENGTH_SHORT).show();
                }
                //myRef.setValue(lembab);
            }
        });

        setTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String waktu = time.getText().toString();
                //int angkawaktu = Integer.parseInt(waktu);
                //TODO something when floating action menu first item clicked
                myRef.child("set_waktu").setValue(waktu);
                //database.getReference("set_waktu").setValue(waktu);
                if (time.getText().toString().length() == 0)
                    time.setError("Harus diisi");

                if (TextUtils.isEmpty(waktu)) {
                    Toast.makeText(getApplicationContext(), "Masukan Pengaturan Waktu !", Toast.LENGTH_SHORT).show();
                    requestFocus(time
                    );
                    return;
                } else {
                    Toast.makeText(getApplicationContext(), "Pengaturan Penyiraman Untuk Waktu " + waktu + " Berhasil", Toast.LENGTH_SHORT).show();
                }

                //myRef.setValue(lembab);
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Calendar mcurrentTime = Calendar.getInstance();
              int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
              int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        time.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        //database.getReference("baca_suhu").addValueEventListener(new ValueEventListener() {
        mFirebaseInstance.getReference("tomat").child("baca_suhu").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "Suhu Updated");

                final Float suhu = dataSnapshot.getValue(Float.class);
                String s = Float.toString(suhu);
                //angkasuhu = Integer.parseInt(suhu);
                statussuhu.setText(s);
                setResume(suhu,kelembaban);
                Log.e("SSSS", "xx");

                if (suhu >=0 && suhu <20){
                    statussuhu2.setText("Dingin");
                } else if (suhu >=20 && suhu <25){
                    statussuhu2.setText("Sejuk");
                } else if (suhu >=25 && suhu <30){
                    statussuhu2.setText("Normal");
                } else if (suhu >=30 && suhu <35){
                    statussuhu2.setText("Hangat");
                } else if (suhu >=35){
                    statussuhu2.setText("Panas");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to suhu.", error.toException());
            }
        });

        //database.getReference("baca_temp").addValueEventListener(new ValueEventListener() {
        mFirebaseInstance.getReference("tomat").child("baca_kelembaban").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "Kelembaban Updated");

                Float kelembaban = dataSnapshot.getValue(Float.class);
                //angkakelembaban = Integer.parseInt(kelembaban);
                String s = Float.toString(kelembaban);
                //angkasuhu = Integer.parseInt(suhu);
                if (kelembaban >=0 && kelembaban <300){
                    statuskelembaban2.setText("Kering");
                } else if (kelembaban >=300 && kelembaban <700){
                    statuskelembaban2.setText("Normal");
                } else if (kelembaban >=700){
                    statuskelembaban2.setText("Basah");
                }
                statuskelembaban.setText(s);
                setResume(suhu,kelembaban);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to suhu.", error.toException());
            }
        });
        Log.e("SSSS", "ll");
    }

private void requestFocus(View view) {
    if (view.requestFocus()) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}

public void setResume(float suhu, float kelembaban){
    //status.setText("Suhu : "+ suhu + " Kelembaban : "+ kelembaban);
    //status2.setText("Suhu : "+ kelembaban);
    //                // Add label converter
    proses.setVisibility(View.VISIBLE);
    loading.setVisibility(View.GONE);
    float kelembaban2 = kelembaban/10;
    float hasil = ((suhu+kelembaban2)/2);

    speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
        @Override
        public String getLabelFor(double progress, double maxProgress) {
            return String.valueOf((int) Math.round(progress));
        }
    });
//
//                // configure value range and ticks
    speedometer.setMaxSpeed(100);
    speedometer.setMajorTickStep(10);
    speedometer.setMinorTicks(4);
//
//                // Configure value range colors
    speedometer.addColoredRange(0, 30, Color.RED);
    speedometer.addColoredRange(30, 70, Color.YELLOW);
    speedometer.addColoredRange(70, 100, Color.GREEN);
    speedometer.setSpeed(kelembaban2, 1000, 300);
}

}
