package com.crestron.aurora.viewtesting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import kotlinx.android.synthetic.main.activity_view_testing.*

class ViewTesting : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_testing)

        toggle_button_group.addOnButtonCheckedListener { group, checkedId, isChecked ->
            Loged.r(checkedId)
        }

    }

}
