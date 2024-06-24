package com.example.trialrun;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import de.acdgruppe.m2uhf_library.Enums.MemBank;
import de.acdgruppe.m2uhf_library.Enums.TagOperationType;
import de.acdgruppe.m2uhf_library.Ipj_Error;
import de.acdgruppe.m2uhf_library.JNIAdvancedReporter;
import de.acdgruppe.m2uhf_library.JNIReporter;

public class WriteActivity extends AppCompatActivity {

    private WriteActivity context;
    private EditText tagText;
    private Button btnWrite, btnReload;
    private TextView tagSelected;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        context = this;

        // assign the views
        tagText = findViewById(R.id.edittext_write);
        tagSelected = findViewById(R.id.txtTagSelected);
        btnReload = findViewById(R.id.btn_reload);
        btnWrite = findViewById(R.id.btn_write);
        tagText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // assign a click listener to the write button that handles write operations
        btnWrite.setOnClickListener(v -> {

            String[] hexStringArr = tagText.getText().toString().split("-");
            // check if the data provided by the user is valid
            if (HelperFunctions.validateHexWord(hexStringArr)) {
                int[] hexArr = new int[hexStringArr.length];
                for (int i = 0; i < hexStringArr.length; i++) {
                    hexArr[i] = Integer.parseInt(hexStringArr[i], 16);
                }

                String[] hexStringArrToFind = tagSelected.getText().toString().split("-");
                int[] hexArrToFind = new int[hexStringArrToFind.length];
                for (int i = 0; i < hexStringArrToFind.length; i++) {
                    hexArrToFind[i] = Integer.parseInt(hexStringArrToFind[i], 16);
                }

                // the memory bank can be defined here, as an example EPC is used
                MemBank memBank = MemBank.EPC;

                // the index where the data is written to can be defined here, as an example 2 is used
                int index = 2;

                // run the library call in a new thread to avoid blocking the main thread
                Runnable runnable = () -> {
                    Ipj_Error res = AppContext2.Get().getM2uhfLib().writeTag(hexArrToFind, false, hexArr, memBank, index, 0);
                    // the result just tells if the write function was started correctly,
                    // a tagOperationReport is sent via the JNIAdvancedReporter if the write was completed
                    Log.d(AppContext2.TAG, "writeTagResult " + res.toString());
                };
                new Thread(runnable).start();
            } else {
                new HelperFunctions().showErrorDialog(context, "Invalid input data", Ipj_Error.GENERAL_ERROR);
            }
        });

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
                context.runOnUiThread(() -> {
                    if (!epcMem.isEmpty()) {
                        Log.d(AppContext2.TAG, "Tag found: " + epcMem);
                        tagSelected.setText(epcMem);
                        tagText.setText(epcMem);
                        if (isScanning) {
                            isScanning = false;
                            setBtnStatus(true);

                            // stop the scan when a tag is found
                            AppContext2.Get().getM2uhfLib().stopReader();
                        }
                    }
                });
            }

            @Override
            public void onTagStatus(int reversePower) {
                // Watch out! Do not block this call here since it will block the c library, always use threads
            }

            @Override
            public void onAutostop() {
                // enable the reload button after the scan was stopped when no tag was found
                runOnUiThread(() -> btnReload.setEnabled(true));
            }
        };

        JNIAdvancedReporter advListener = tagOperationReport -> {
            // If the tag was removed from the field, there will be no report -> use a timer to show an error

            // There are multiple cases a report is sent, to differ between them, use "tagOperationType"
            boolean wasWriteOperation = tagOperationReport.hasTagOperationType && (tagOperationReport.tagOperationType == TagOperationType.WRITE
                    || tagOperationReport.tagOperationType == TagOperationType.WRITE_EPC);

            // If there was an error
            if (tagOperationReport.hasError && tagOperationReport.ipj_error != Ipj_Error.SUCCESS) {
                new HelperFunctions().showErrorDialog(context, "Error while writing", tagOperationReport.ipj_error);
            } else {
                Toast.makeText(context, "Write was successful", Toast.LENGTH_LONG).show();
            }

            // reload the tag, the epc might have changed
            reload();

            Log.d(AppContext2.TAG, "writeTask finished");
        };

        // reload a new tag when this Button is pressed
        btnReload.setOnClickListener(view -> reload());

        AppContext2.Get().getM2uhfLib().setAdvancedCallback(advListener);
        AppContext2.Get().getM2uhfLib().setCallback(nlistener);
        // load the first tag after everything is initialized
        reload();
    }

    private void setBtnStatus(boolean enable) {
        btnWrite.setEnabled(enable);
        btnReload.setEnabled(enable);
    }

    private void reload() {
        // scan for a tag in a new thread to avoid blocking the main thread
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (AppContext2.Get().getM2uhfLib().isInitialized()) {
                isScanning = true;
                setBtnStatus(false);
                AppContext2.Get().getM2uhfLib().startReader();
            }
        }, 5);
    }

    @Override
    public void onPause() {
        // react to the activity pausing
        if (!btnReload.isEnabled()) {
            final Handler handler = new Handler();
            handler.post(() -> AppContext2.Get().getM2uhfLib().stopReader());
        }
        super.onPause();
    }
}