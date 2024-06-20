package com.example.trialrun

import android.os.Bundle
//import androidx.activity.ComponentActivity

import android.os.Handler
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import de.acdgruppe.m2uhf_library.Enums.MemBank
//import de.acdgruppe.m2uhf_library.Ipj_Error
//import de.acdgruppe.m2uhf_library.JNIAdvancedReporter
//import de.acdgruppe.m2uhf_library.JNIReporter


class WriteActivity : AppCompatActivity() {
    private lateinit var context: WriteActivity
    private lateinit var tagText: EditText
    private lateinit var btnWrite: Button
    private lateinit var btnReload: Button
    private lateinit var tagSelected: TextView
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        context = this

        // assign the views
        tagText = findViewById(R.id.edittext_write)
        tagSelected = findViewById(R.id.txtTagSelected)
        btnReload = findViewById(R.id.btn_reload)
        btnWrite = findViewById(R.id.btn_write)
        tagText.setFilters(arrayOf<InputFilter>(AllCaps()))

        // assign a click listener to the write button that handles write operations
        btnWrite.setOnClickListener(View.OnClickListener { v: View? ->
            val hexStringArr =
                tagText.getText().toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            // check if the data provided by the user is valid
            if (HelperFunctions.validateHexWord(hexStringArr)) {
                val hexArr = IntArray(hexStringArr.size)
                for (i in hexStringArr.indices) {
                    hexArr[i] = hexStringArr[i].toInt(16)
                }

                val hexStringArrToFind = tagSelected.getText().toString().split("-".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val hexArrToFind = IntArray(hexStringArrToFind.size)
                for (i in hexStringArrToFind.indices) {
                    hexArrToFind[i] = hexStringArrToFind[i].toInt(16)
                }

                // the memory bank can be defined here, as an example EPC is used
                //val memBank = MemBank.EPC

                // the index where the data is written to can be defined here, as an example 2 is used
                val index = 2

                // run the library call in a new thread to avoid blocking the main thread
                //val runnable = Runnable {
                    //val res = AppContext.Get().m2uhfLib.writeTag(
                       // hexArrToFind,
                        //false,
                       // hexArr,
                       // memBank,
                      //  index,
                      //  0
                   // )
                    // the result just tells if the write function was started correctly,
                    // a tagOperationReport is sent via the JNIAdvancedReporter if the write was completed
                   // Log.d(AppContext.TAG, "writeTagResult $res")
                }
               // Thread(runnable).start()
           // } else {
               // HelperFunctions().showErrorDialog(
                    //context,
                    //"Invalid input data",
                  //  Ipj_Error.GENERAL_ERROR
               // )
         //   }
       // })

    })
    }
}
