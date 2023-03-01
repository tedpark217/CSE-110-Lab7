package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import edu.ucsd.cse110.sharednotes.view.NotesAdapter;

public class NoteRepository {
    private final NoteDao dao;
    private TimeService timeService;
    private NoteAPI server;

    public NoteRepository(NoteDao dao) {
        this.dao = dao;
        this.server = new NoteAPI();
    }

    // Synced Methods
    // ==============

    /**
     * This is where the magic happens. This method will return a LiveData object that will be
     * updated when the note is updated either locally or remotely on the server. Our activities
     * however will only need to observe this one LiveData object, and don't need to care where
     * it comes from!
     *
     * This method will always prefer the newest version of the note.
     *
     * @param title the title of the note
     * @return a LiveData object that will be updated when the note is updated locally or remotely.
     */
    public LiveData<Note> getSynced(String title) {
        var note = new MediatorLiveData<Note>();

        Observer<Note> updateFromRemote = theirNote -> {
            var ourNote = note.getValue();
            if (ourNote == null || ourNote.updatedAt < theirNote.updatedAt) {
                upsertLocal(theirNote);
            }
        };

        // If we get a local update, pass it on.
        note.addSource(getLocal(title), note::postValue);
        // If we get a remote update, update the local version (triggering the above observer)
        note.addSource(getRemote(title), updateFromRemote);

        return note;
    }

    public void upsertSynced(Note note) {
        upsertLocal(note);
        upsertRemote(note);
    }

    // Local Methods
    // =============

    public LiveData<Note> getLocal(String title) {
        return dao.get(title);
    }

    public LiveData<List<Note>> getAllLocal() {
        return dao.getAll();
    }

    public void upsertLocal(Note note) {
        note.updatedAt = System.currentTimeMillis();
        dao.upsert(note);
    }

    public void deleteLocal(Note note) {
        dao.delete(note);
    }

    public boolean existsLocal(String title) {
        return dao.exists(title);
    }

    // Remote Methods
    // ==============

    public LiveData<Note> getRemote(String title) {
        // TODO: Implement getRemote!
        // TODO: Set up polling background thread (MutableLiveData?)
        // TODO: Refer to TimerService from https://github.com/DylanLukes/CSE-110-WI23-Demo5-V2.

        // Start by fetching the note from the server _once_ and feeding it into MutableLiveData.
        MutableLiveData<Note> note = new MutableLiveData<>();

        TimestampAdapter adapter = new TimestampAdapter();


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            note.setValue(server.get(title));
        });

        // Then, set up a background thread that will poll the server every 3 seconds.

        /**
        timeService.getTime().observe(this, time -> {
            executor.submit(() -> {
                server.get(title);
            });
        });**/

        // You may (but don't have to) want to cache the LiveData's for each title, so that
        // you don't create a new polling thread every time you call getRemote with the same title.
        // You don't need to worry about killing background threads.

        return note;
    }

    public void upsertRemote(Note note) {
        // TODO: Implement upsertRemote!
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            server.put(note);
        });
    }
}
