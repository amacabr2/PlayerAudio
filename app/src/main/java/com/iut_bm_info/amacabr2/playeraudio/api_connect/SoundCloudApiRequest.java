package com.iut_bm_info.amacabr2.playeraudio.api_connect;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.iut_bm_info.amacabr2.playeraudio.config.Config;
import com.iut_bm_info.amacabr2.playeraudio.models.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.Method.GET;

/**
 * Created by amacabr2 on 23/11/17.
 */

public class SoundCloudApiRequest implements Response.Listener<JSONArray>, Response.ErrorListener {

    private static final String URL = "https://api.soundcloud.com/tracks?client_id=" + Config.CLIENT_ID + "&filter=public&limit=100";

    private RequestQueue requestQueue;

    private SoundCloudInterface callback;

    public SoundCloudApiRequest(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void getSongs(String query, SoundCloudInterface callback) {
        String url = URL;
        if (query.length() > 0) {
            try {
                query = URLEncoder.encode(query, "UTF-8");
                url = URL + "&q=" + query;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        this.callback = callback;
        JsonArrayRequest request = new JsonArrayRequest(GET, url, this, this);
        requestQueue.add(request);
    }

    @Override
    public void onResponse(JSONArray response) {
        List<Song> songs = new ArrayList<>();

        if (response.length() > 0) {
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject songObject = response.getJSONObject(i);
                    long id = songObject.getLong("id");
                    String title = songObject.getString("title");
                    String artworkUrl = songObject.getString("atwork_url");
                    String streamUrl = songObject.getString("stream_url");
                    long duration = songObject.getLong("duration");
                    int playbackCount = songObject.has("playback_count") ? songObject.getInt("playback_count") : 0;
                    JSONObject userObject = songObject.getJSONObject("user");
                    String artist = userObject.getString("username");

                    songs.add(new Song(id, title, artist, artworkUrl, duration, streamUrl, playbackCount));
                } catch (JSONException e) {
                    callback.onError("Une erreur est survenue");
                }
            }

            callback.onSuccess(songs);
        } else {
            callback.onError("Aucune chanson trouvé");
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        callback.onError("Une erreur est survenue");
    }

    public interface SoundCloudInterface {

        void onSuccess(List<Song> songs);

        void onError(String message);
    }
}
