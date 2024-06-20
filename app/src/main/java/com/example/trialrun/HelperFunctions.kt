package com.example.trialrun

import android.app.AlertDialog
import android.content.Context
//import com.example.trialrun.m2uhf_library.Ipj_Error

class HelperFunctions {

    //fun showErrorDialog(context: Context, message: String, errorCode: Ipj_Error?) {
     //   val alertDialogBuilder = AlertDialog.Builder(context)

      //  alertDialogBuilder.setMessage(
       //     if (errorCode != null) {
       //         "$message\nError code: $errorCode"
      //      } else {
      //          message
      //      }
     //   )
     //   alertDialogBuilder.setTitle("An error occurred!")
     //   alertDialogBuilder.setNegativeButton("Ok") { dialog, _ -> dialog.dismiss() }
     //   val alertDialog = alertDialogBuilder.create()
      //  alertDialog.show()
    //}

    companion object {
        fun validateHexWord(hexStringArray: Array<String>): Boolean {
            for (hexValue in hexStringArray) {
                if (!checkHex(hexValue) || hexValue.length != 4) {
                    return false
                }
            }
            return true
        }

        private fun checkHex(s: String): Boolean {
            // Size of string
            val n = s.length

            // Iterate over string
            for (i in 0 until n) {
                val ch = s[i]

                // Check if the character is invalid
                if ((ch < '0' || ch > '9') && (ch < 'A' || ch > 'F')) {
                    return false
                }
            }
            return true
        }
    }
}

