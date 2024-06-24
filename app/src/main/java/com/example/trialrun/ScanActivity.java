package com.example.trialrun;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.ToggleButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trialrun.AppContext2;
import com.example.trialrun.HelperFunctions;

import de.acdgruppe.m2uhf_library.Ipj_Error;
import de.acdgruppe.m2uhf_library.JNIReporter;
import com.example.trialrun.tag.Tag;
import com.example.trialrun.R.*;
import com.example.trialrun.tag.TagAdapter;


public class ScanActivity extends AppCompatActivity {

    RecyclerView tagView;
    private ScanActivity context;
    private TagAdapter localAdapter;
    private ProgressBar progressBar;
    ToggleButton btn_start;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        context = this;
        progressBar = findViewById(R.id.progressBar);
        localAdapter = new TagAdapter();
        tagView = findViewById(R.id.listview);
        tagView.setHasFixedSize(true);
        tagView.setItemAnimator(null);
        tagView.setLayoutManager(new LinearLayoutManager(context));
        tagView.setAdapter(localAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(context, VERTICAL);
        tagView.addItemDecoration(itemDecor);
        btn_start = findViewById(R.id.btn_start);

        // listener that handles incoming scan data
        JNIReporter nlistener = new JNIReporter() {
            @Override
            public void onStatusMessage(Ipj_Error errorCode) {
                // Watch out! Do not block this call here since it will block the c library, always use threads
                Log.d(AppContext2.TAG, "onStatusMessage: " + errorCode.toString());
            }

            @Override
            public void onTagUpdate(String epcMem, String tagId, int rssi, int phase, int pc) {
                // Watch out! Do not block this call here since it will block the c library, always use threads
                // the tags in range have changed
                if (!epcMem.isEmpty()) {
                    localAdapter.checkTag(new Tag(epcMem));
                }
            }

            @Override
            public void onTagStatus(int reversePower) {
                // Watch out! Do not block this call here since it will block the c library, always use threads
            }

            @Override
            public void onAutostop() {
                // Watch out! Do not block this call here since it will block the c library, always use threads
                // to avoid overheating of the module, it stops automatically after two minutes. When this happens you can handle it here
            }
        };

        // assign variables
        AppContext2.Get().getM2uhfLib().setCallback(nlistener);
        AppContext2.Get().getM2uhfLib().setAdvancedCallback(null);

        // when the start button is pressed it starts the scan
        btn_start.setOnCheckedChangeListener((buttonView, isChecked) -> {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = () -> {
                if (isChecked) {
                    if (AppContext2.Get().getM2uhfLib().isInitialized()) {
                        // run the library call in a new thread to avoid blocking the main thread (can be simplified in separate class)
                        Runnable runnable = () -> AppContext2.Get().getM2uhfLib().stopReader();
                        new Thread(runnable).start();
                        Log.d(AppContext2.TAG, "Reader stopped");
                    }
                } else {
                    if (AppContext2.Get().getM2uhfLib().isInitialized()) {
                        // run the library call in a new thread to avoid blocking the main thread (can be simplified in separate class)
                        Runnable runnable = () -> AppContext2.Get().getM2uhfLib().startReader();
                        new Thread(runnable).start();
                        Log.d(AppContext2.TAG, "Reader started");
                    } else {
                        btn_start.setChecked(false);
                        new HelperFunctions().showErrorDialog(context, "Reader is not initialized!", null);
                    }
                }
                localAdapter.removeOldTags(10000);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            };
            mainHandler.post(myRunnable);
        });
    }

    @Override
    public void onPause() {
        // react to the activity pausing
        if (!btn_start.isChecked()) {
            final Handler handler = new Handler();
            handler.post(() -> AppContext2.Get().getM2uhfLib().stopReader());
        }
        super.onPause();
    }
}