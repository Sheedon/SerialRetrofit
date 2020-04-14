package org.sheedon.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.sheedon.demo.converters.DataConverterFactory;
import org.sheedon.serial.SerialClient;
import org.sheedon.serial.retrofit.Call;
import org.sheedon.serial.retrofit.Callback;
import org.sheedon.serial.retrofit.Observable;
import org.sheedon.serial.retrofit.Response;
import org.sheedon.serial.retrofit.Retrofit;
import org.sheedon.serial.retrofit.converters.SerialConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SerialClient client = new SerialClient.Builder()
                .path("/tty/s4")
                .baudRate(9600)
                .name("qrcode")
                .addConverterFactory(DataConverterFactory.create())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .addConverterFactory(SerialConverterFactory.create())
                .baseStartBit("7A")
                .baseEndBit("")
                .build();

        final RemoteService remoteService = retrofit.create(RemoteService.class);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<BoxModel> managerList = remoteService.getManagerList("0800", "02", "03", "01");
                managerList.enqueue(new Callback.Call<BoxModel>() {
                    @Override
                    public void onResponse(Call<BoxModel> call, Response<BoxModel> response) {
                        Log.v("SXD",""+response.body());
                    }

                    @Override
                    public void onFailure(Call<BoxModel> call, Throwable t) {
                        Log.v("SXD",""+t);
                    }
                });

            }
        });

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable<BoxModel> managerList = remoteService.getManagerList1();
                managerList.subscribe(new Callback.Observable<BoxModel>() {
                    @Override
                    public void onResponse(Observable<BoxModel> call, Response<BoxModel> response) {
                        Log.v("SXD", "" + response.body());
                    }

                    @Override
                    public void onFailure(Observable<BoxModel> call, Throwable t) {
                        Log.v("SXD", "" + t);
                    }
                });
            }
        });
    }
}
