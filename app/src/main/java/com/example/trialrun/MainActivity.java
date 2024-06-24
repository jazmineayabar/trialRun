package com.example.trialrun;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.google.android.material.checkbox.MaterialCheckBox;

import de.acdgruppe.m2uhf_library.BuildConfig;
import de.acdgruppe.m2uhf_library.Ipj_Error;
import de.acdgruppe.m2uhf_library.Ipj_Key;
import de.acdgruppe.m2uhf_library.ModuleStatusReporter;
import com.example.trialrun.R.*;


public class MainActivity extends AppCompatActivity {

    ToggleButton btn_init, btn_power;
    Button btn_startScanActivity, btn_startWriteActivity;
    private ProgressBar progressBar;
    private Context context;
    private MaterialCheckBox cbxPluggedStatus, cbxPowerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        // setup the Toolbar to display the App version defined in the apps build.gradle file
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView txtVersion = toolbar.findViewById(R.id.txtAppVersion);
        String versionInfo;
        String version = BuildConfig.VERSION_NAME;
        versionInfo = String.format(getString(R.string.version), version);
        txtVersion.setText(versionInfo);

        // get the views of all the interactive UI elements
        progressBar = findViewById(R.id.progressBar);
        cbxPowerStatus = findViewById(R.id.cbxPowerStatus);
        cbxPluggedStatus = findViewById(R.id.cbxPluggedStatus);
        btn_startScanActivity = findViewById(R.id.startScanActivity);
        btn_startWriteActivity = findViewById(R.id.startWriteActivity);
        btn_power = findViewById(R.id.btn_power);
        btn_init = findViewById(R.id.btn_init);

        // ensure the app has the permission to access the external storage
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permissions so prompt the user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // assign values to some variables of the AppContext class
        AppContext2.Get().setContext(this);
        AppContext2.Get().setModuleStatusReporter(statusReporter);

        // assign listeners to the interactive UI elements
        btn_power.setOnCheckedChangeListener((buttonView, isChecked) -> {
            progressBar.setVisibility(ProgressBar.VISIBLE);

            // switch the power of the module according to the power buttons state
            if (isChecked) {
                if (!AppContext2.Get().getM2uhfModule().isPowered()) {
                    AppContext2.Get().getM2uhfModule().powerOn();
                }
                progressBar.setVisibility(ProgressBar.INVISIBLE);

            } else {
                if (AppContext2.Get().getM2uhfModule().isPowered()) {
                    // Always deinit the reader before powering off the module
                    AppContext2.Get().getM2uhfLib().deInit();
                    AppContext2.Get().getM2uhfModule().powerOff();
                }
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                btn_init.setChecked(true);
            }
        });

        btn_init.setOnCheckedChangeListener((buttonView, isChecked) -> {
            progressBar.setVisibility(ProgressBar.VISIBLE);

            // initialize or deinitialize the module according to the initialize buttons state
            if (isChecked) {
                if (AppContext2.Get().getM2uhfModule().isPowered() && !AppContext2.Get().getM2uhfLib().isInitialized()) {
                    // start the initialisation task
                    new readerInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    setButtonStatus(true);
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            } else {
                if (AppContext2.Get().getM2uhfModule().isPowered() && AppContext2.Get().getM2uhfLib().isInitialized()) {
                    // deinitialize the module inside a new thread to avoid blocking the UI
                    Runnable libRunnable = () -> {
                        AppContext2.Get().getM2uhfLib().deInit();
                        AppContext2.Get().reset();
                        // access the UI to update the button status
                        runOnUiThread(() -> setButtonStatus(false));
                    };
                    new Thread(libRunnable).start();
                }
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });

        View.OnClickListener startActivityListener = view -> {
            // start the activity that is corresponding to the button that was pressed
            Intent myIntent = null;
            if (view.getId() == R.id.startScanActivity) {
                myIntent = new Intent(context, ScanActivity.class);
            } else if (view.getId() == R.id.startWriteActivity) {
                myIntent = new Intent(context, WriteActivity.class);
            }

            if (myIntent != null) {
                context.startActivity(myIntent);
            }
        };
        btn_startScanActivity.setOnClickListener(startActivityListener);
        btn_startWriteActivity.setOnClickListener(startActivityListener);

        // initialize the module class
        moduleInit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // react to the application being paused
        // It might be a good idea to stop the reader here if it is still running and was not stopped in other activities
        // To save power, the module can be powered off when the app is paused.
        // To power the module on again, use the "onResume()" override
    }

    @Override
    protected void onDestroy() {
        // react to the application being destroyed
        AppContext2.Get().getM2uhfModule().deInit();

        super.onDestroy();
    }

    private void setButtonStatus(boolean enable) {
        btn_startScanActivity.setEnabled(enable);
        btn_startWriteActivity.setEnabled(enable);
    }

    // the ACD ModuleService will send events when a module is plugged or powered
    // these events can be received here
    ModuleStatusReporter statusReporter = new ModuleStatusReporter() {
        @Override
        public void onModulePlugged(boolean pluggedIn) {
            // a module was plugged in or out
            Log.d(AppContext2.TAG, "pluggedIn: " + pluggedIn);

            cbxPluggedStatus.setChecked(pluggedIn);
            if (!pluggedIn) {
                btn_init.setEnabled(false);
            }

            btn_power.setEnabled(pluggedIn);
            setButtonStatus(false);
        }

        @Override
        public void onModulePowered(boolean poweredOn) {
            // a module was powered on or off
            Log.d(AppContext2.TAG, "poweredOn: " + poweredOn);
            btn_power.setChecked(poweredOn);
            btn_init.setEnabled(poweredOn);
            cbxPowerStatus.setChecked(poweredOn);
            if (!poweredOn) {
                btn_init.setChecked(true);
                setButtonStatus(false);
            }
        }

        @Override
        public void onModuleError(Ipj_Error ipj_error) {
            // react to a module error
            Log.d(AppContext2.TAG, "onModuleError " + ipj_error.toString());
        }
    };

    // initialize the module class
    // this is needed to create a connection to ACD ModuleService for power management of the module
    private void moduleInit() {
        // Init the module class in a separate thread
        Runnable runnable = () -> {
            AppContext2.Get().getM2uhfModule();
            // Start a new thread for UI changes
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = () -> {
                // enable the UI
                cbxPluggedStatus.setChecked(AppContext2.Get().getM2uhfModule().isPlugged());
                cbxPowerStatus.setChecked(AppContext2.Get().getM2uhfModule().isPowered());
                btn_power.setEnabled(AppContext2.Get().getM2uhfModule().isPlugged());
                btn_init.setEnabled(AppContext2.Get().getM2uhfModule().isPowered());
            };
            mainHandler.post(myRunnable);
        };
        new Thread(runnable).start();
    }

    // task to initialize the rfid reader itself.
    // This happens after the moduleInitTask was finished and only when a module is plugged
    private class readerInitTask extends AsyncTask<Void, Void, Ipj_Error> {

        @Override
        protected Ipj_Error doInBackground(Void... params) {
            // This will init the reader, a connection to the reader chip will be established
            Ipj_Error res = AppContext2.Get().getM2uhfLib().init();
            // After the init was successful, other API functions can be called
            // as example, the tx power settings can be changed:
            // res = AppContext.Get().getM2uhfLib().set().TxPower(23.0f);

            Log.d(AppContext2.TAG, "Current TX power: " + AppContext2.Get().getM2uhfLib().get().keyValue(Ipj_Key.ANTENNA_TX_POWER));
            Log.d(AppContext2.TAG, "API version: " + AppContext2.Get().getM2uhfLib().get().Version());
            return res;
        }

        @Override
        protected void onPostExecute(Ipj_Error result) {
            // update the UI corresponding to the result
            if (result != Ipj_Error.SUCCESS) {
                Log.d(AppContext2.TAG, "setEnabled(false)");
                btn_init.setChecked(true);
                setButtonStatus(false);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable myRunnable = () -> new HelperFunctions().showErrorDialog(context, "Init failed", result);
                mainHandler.post(myRunnable);
            } else {
                Log.d(AppContext2.TAG, "setEnabled(true)");
                setButtonStatus(true);
            }
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }
}
