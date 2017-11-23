package com.iut_bm_info.amacabr2.playeraudio.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.iut_bm_info.amacabr2.playeraudio.R;
import com.iut_bm_info.amacabr2.playeraudio.api_connect.SoundCloudApiRequest;
import com.iut_bm_info.amacabr2.playeraudio.api_connect.VolleySingleton;
import com.iut_bm_info.amacabr2.playeraudio.models.Song;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSongs();
    }

    public void getSongs() {
        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        SoundCloudApiRequest request = new SoundCloudApiRequest(queue);

        request.getSongs(new SoundCloudApiRequest.SoundCloudInterface() {
            @Override
            public void onSuccess(List<Song> songs) {

            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
