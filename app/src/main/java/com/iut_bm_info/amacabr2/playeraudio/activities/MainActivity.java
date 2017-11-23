package com.iut_bm_info.amacabr2.playeraudio.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.iut_bm_info.amacabr2.playeraudio.R;
import com.iut_bm_info.amacabr2.playeraudio.adapters.SongAdapter;
import com.iut_bm_info.amacabr2.playeraudio.api_connect.SoundCloudApiRequest;
import com.iut_bm_info.amacabr2.playeraudio.api_connect.VolleySingleton;
import com.iut_bm_info.amacabr2.playeraudio.models.Song;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SongAdapter.RecyclerItemClickListener {

    private RecyclerView recycler;

    private SongAdapter adapter;

    private List<Song> songs;

    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        setContentView(R.layout.activity_main);
        getSongs();

        songs = new ArrayList<>();

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new SongAdapter(getApplicationContext(), songs, this);
        recycler.setAdapter(adapter);
    }

    public void initializeViews() {
        recycler = findViewById(R.id.mainActivity_recycler);
    }

    public void getSongs() {
        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        SoundCloudApiRequest request = new SoundCloudApiRequest(queue);

        request.getSongs(new SoundCloudApiRequest.SoundCloudInterface() {
            @Override
            public void onSuccess(List<Song> newSongs) {
                currentIndex = 0;
                songs.addAll(newSongs);
                adapter.notifyDataSetChanged();
                adapter.setSelectedPosition(0);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClickListener(Song song, int position) {
        Toast.makeText(this, song.getTitle(), Toast.LENGTH_SHORT).show();
        changeSelectedSong(position);
    }

    private void changeSelectedSong(int index) {
        adapter.notifyItemChanged(adapter.getSelectedPosition());
        currentIndex = index;
        adapter.setSelectedPosition(currentIndex);
        adapter.notifyItemChanged(currentIndex);
    }
}
