package edu.ucsd.cse110.sharednotes.model;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // Read the docs: https://square.github.io/okhttp/
    // Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Note get(String title) {
        // URLs cannot contain spaces, so we replace them with %20.
        title = title.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();

            if("{\"detail\":\"Note not found.\"}".equals(body)){
                return new Note(title, "");
            }
            Log.d("GET", body);
            return Note.fromJSON(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Note(title, "");
    }

    public void put(Note n) {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        Gson gson = new Gson();
        var map = Map.of("content", n.content, "updated_at", n.updatedAt);
        String s = gson.toJson(map);
        var body = RequestBody.create(s, JSON);
        var title = n.title.replace(" ", "%20");
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("PUT", body)
                .build();

        try(var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var b = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
