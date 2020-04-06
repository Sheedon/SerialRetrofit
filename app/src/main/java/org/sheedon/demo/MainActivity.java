package org.sheedon.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.sheedon.demo.converters.DataConverterFactory;
import org.sheedon.serial.SerialClient;
import org.sheedon.serial.retrofit.Call;
import org.sheedon.serial.retrofit.Callback;
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
                .name("qrcode")
                .addConverterFactory(DataConverterFactory.create())
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
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
                managerList.enqueue(new Callback<BoxModel>() {
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
    }
}
