package com.crestron.aurora

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.thejuki.kformmaster.helper.*
import com.thejuki.kformmaster.model.FormMultiLineEditTextElement
import com.thejuki.kformmaster.model.FormSingleLineEditTextElement
import com.thejuki.kformmaster.model.FormSliderElement
import kotlinx.android.synthetic.main.activity_form.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request


class FormActivity : AppCompatActivity() {

    private lateinit var forms: FormBuildHelper

    @SuppressLint("SetJavaScriptEnabled", "PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(android.R.style.Theme_Material_Light)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        val forms = form(this, recyclerViewForm) {
            header {
                title = "Feedback"
            }
            textArea(1) {
                title = "Feedback"
                maxLines = 3
                hint = "Enter Feedback Here"
                required = true
                //rightToLeft = false
                error = "Please enter feedback"
                //titleTextColor = getColor(R.color.chevronBgColor)
                //valueTextColor = Color.WHITE
                //backgroundColor = getColor(R.color.background_material_dark)
            }
            /*header {
                title = "Did You Like The App?"
                collapsible = true
            }*/
            slider(2) {
                title = "Did You Like The App?"
                value = 5
                max = 10
                min = 0
                incrementBy = 1
                //titleTextColor = getColor(R.color.niceBlue)
                //valueTextColor = getColor(R.color.niceBlue)
                //backgroundColor = getColor(R.color.background_material_dark)

                valueObservers.add { newValue, element ->
                    val choice = when (newValue) {
                        0 -> "Despise it!"
                        1 -> "Hate it!"
                        2 -> "Don't like it."
                        3 -> "I've used better"
                        4 -> "Not the worse"
                        5 -> "Not bad but could be better"
                        6 -> "Not bad"
                        7 -> "I like it"
                        8 -> "It pretty good"
                        9 -> "It is really well done!"
                        10 -> "It is the best downloader ever!"
                        else -> "Meh"
                    }
                    element.title = "Did You Like The App? $choice"
                }

                //rightToLeft = false
            }
            /*header {
                title = "Name (If you want to, leave your name so I can give credit in future updates.)"
                collapsible = true
            }*/
            text(3) {
                title = "Name (If you want to, leave your name so I can give credit in future updates.)"
                //hint = "Name (If you want to, leave your name so I can give credit in future updates.)"
                hint = "Enter here"
                //rightToLeft = false
                //backgroundColor = getColor(R.color.background_material_dark)
                //valueTextColor = Color.WHITE
            }
            button(4) {
                //backgroundColor = getColor(R.color.background_material_dark)
                displayDivider = true
                value = "Submit"
                valueObservers.add { _, _ ->
                    val feed = this@form.getFormElement<FormMultiLineEditTextElement>(1)
                    val rating = this@form.getFormElement<FormSliderElement>(2)
                    val name = this@form.getFormElement<FormSingleLineEditTextElement>(3)
                    if (this@form.isValidForm)
                        finalSubmit(feed.valueAsString, rating.valueAsString, name.valueAsString)
                    else {
                        Toast.makeText(this@FormActivity, "Please complete the required fields.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun finalSubmit(feedback: String, rating: String, name: String?) = launch(UI) {
        val req = submit(feedback, rating, name)
        if (req.await()) {
            finish()
        } else {
            Toast.makeText(this@FormActivity, "Submitting Failed. Please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submit(feedback: String, rating: String, name: String?) = async {
        val url = "https://docs.google.com/forms/d/e/1FAIpQLSc27pWo2vM1MSXR3KrykkM6V0LLti4I4jJjfMxOZAEuSRol9g/formResponse"
        val feedbackID = "entry.49490076"
        val levelID = "entry.23666763"
        val nameID = "entry.1685416350"

        val form = FormBody.Builder()
                .add(feedbackID, feedback)
                .add(levelID, rating)
                .add(nameID, "$name")
                .build()

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .post(form)
                .build()

        val response = client.newCall(request).execute()

        Loged.i("${response.isSuccessful}")
        response.isSuccessful
    }
}
