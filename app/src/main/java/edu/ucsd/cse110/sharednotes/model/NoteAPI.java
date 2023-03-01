package edu.ucsd.cse110.sharednotes.model;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;

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
            Log.d("GET", body); //gets the correct responsebody
            // TODO: NEED TO CHANGE STRING INTO NOTE
            // didn't get it to work yet
            return new Note("newPost", "newPost", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void put(Note n) {
        Log.d("PUTTING title", n.title);
        Log.d("PUTTING content", n.content);
        Log.d("PUTTING updatedAt", String.valueOf(n.updatedAt));
        // correctly gets the Note to upsert to Remote

        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        Gson gson = new Gson();
        var body = RequestBody.create(gson.toJson(n), JSON);
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + n.title)
                .method("PUT", null)
                .post(body)
                .build();

        try(var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var b = response.body().string();
            Log.d("PUT", b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
