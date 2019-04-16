package com.crestron.aurora

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.crestron.aurora.utilities.Utility
import kotlinx.android.synthetic.main.activity_terminal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.async

class TerminalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)

        runButton.setOnClickListener {
            Loged.wtf(editText.text.toString())
            fun a() = GlobalScope.async(Dispatchers.Main) {
                var s: String
                try {
                    s = Utility.runAsRoot(editText.text.toString())
                } catch (e: Exception) {
                    s = "command not found: " + editText.text.toString()
                    Log.e(packageName, e.toString())
                }
                termInfo.text = s
            }
            a()
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(this)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        editText.setOnEditorActionListener { _, _, _ ->
            runButton.performClick()
        }

        backButtonTerm.setOnClickListener {
            finish()
        }

    }

}
