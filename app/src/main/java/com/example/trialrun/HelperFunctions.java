package com.example.trialrun;

import android.app.AlertDialog;
import android.content.Context;
import de.acdgruppe.m2uhf_library.Ipj_Error;

class HelperFunctions {

    public void showErrorDialog(Context context, String message, Ipj_Error errorCode) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        if (errorCode != null) {
            alertDialogBuilder.setMessage(message + "\nError code: " + errorCode);
        } else {
            alertDialogBuilder.setMessage(message);
        }
        alertDialogBuilder.setTitle("An error occurred!");
        alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static boolean validateHexWord(String[] hexStringArray) {
        for (String hexValue : hexStringArray) {
            if (!checkHex(hexValue) || hexValue.length() != 4) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkHex(String s) {
        // Size of string
        int n = s.length();

        // Iterate over string
        for (int i = 0; i < n; i++) {

            char ch = s.charAt(i);

            // Check if the character is invalid
            if ((ch < '0' || ch > '9')
                    && (ch < 'A' || ch > 'F')) {
                return false;
            }
        }
        return true;
    }
}
