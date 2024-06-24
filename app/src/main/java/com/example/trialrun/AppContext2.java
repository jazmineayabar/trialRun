package com.example.trialrun;

import android.content.Context;
import de.acdgruppe.m2uhf_library.Main;
import de.acdgruppe.m2uhf_library.Module;
import de.acdgruppe.m2uhf_library.ModuleStatusReporter;

public class AppContext2 {

    public static final String TAG = "m2uhf-template";
    private static AppContext2 appContext;
    private static Module m2uhfModule;
    private static Main m2uhfLib;
    private Context context;
    private ModuleStatusReporter moduleStatusReporter;

    // Singelton class to provide data across the entire app
    public static AppContext2 Get() {
        if (appContext == null) {
            // block for other Threads
            synchronized (AppContext2.class) {
                if (appContext == null) {
                    appContext = new AppContext2();
                }
            }
        }
        return appContext;
    }

    public Module getM2uhfModule() {
        if (m2uhfModule == null && context != null && moduleStatusReporter != null) {
            m2uhfModule = new Module(context, moduleStatusReporter);
        }
        return m2uhfModule;
    }

    public Main getM2uhfLib() {
        if (m2uhfLib == null) {
            m2uhfLib = new Main();
        }
        return m2uhfLib;
    }

    public void setModuleStatusReporter(ModuleStatusReporter moduleStatusReporter) {
        this.moduleStatusReporter = moduleStatusReporter;
    }

    public void reset() {
        m2uhfLib = null;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
