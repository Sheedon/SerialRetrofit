package org.sheedon.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.sheedon.demo.converters.DataConverterFactory;
import org.sheedon.demo.converters.SerialConverterFactory;
import org.sheedon.serial.SerialClient;
import org.sheedon.serial.retrofit.Call;
import org.sheedon.serial.retrofit.Callback;
import org.sheedon.serial.retrofit.Observable;
import org.sheedon.serial.retrofit.Response;
import org.sheedon.serial.retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SerialClient client = new SerialClient.Builder()
                .path("/dev/ttyS1")
                .baudRate(115200)
                .name("bridge_rfid")
                .addConverterFactory(DataConverterFactory.create())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .addConverterFactory(SerialConverterFactory.create())
                .baseStartBit("BB")
                .baseEndBit("7E")
                .build();

        final RemoteService remoteService = retrofit.create(RemoteService.class);
        Observable<RFIDModel> observable = remoteService.bindRFID();
        observable.subscribe(new Callback.Observable<RFIDModel>() {
            @Override
            public void onResponse(Observable<RFIDModel> call, Response<RFIDModel> response) {
                Log.v("SXD", "" + response.body());
            }

            @Override
            public void onFailure(Observable<RFIDModel> call, Throwable t) {

            }
        });

        remoteService.bindCommandBack().subscribe(new Callback.Observable<Void>() {
            @Override
            public void onResponse(Observable<Void> call, Response<Void> response) {
                Log.v("SXD", "" + response.body());
            }

            @Override
            public void onFailure(Observable<Void> call, Throwable t) {

            }
        });

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<Void> observable = remoteService.setSignalStrength();
                observable.publishNotCallback();

            }
        });

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<Void> observable = remoteService.sendContinuousRead();
                observable.publishNotCallback();
            }
        });
    }
}
