package com.crestron.aurora.viewtesting

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.crestron.aurora.R
import com.google.android.material.button.MaterialButton
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_testing.*
import kotlinx.android.synthetic.main.material_card_hub_item.view.*
import java.io.File

class ViewTesting : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_testing)

        val mList = arrayListOf(MaterialItem(HubType.STAR, "Solitaire", "Play Solitaire", R.drawable.b_normal),
                MaterialItem(HubType.STAR, "A", "A thing", R.drawable.a_normal),
                MaterialItem(HubType.STAR, "Start", "Start this", R.drawable.start_normal),
                MaterialItem(HubType.STAR, "Ace", "It's an ace", R.drawable.ace1, "Click Me", {
                    Toast.makeText(this@ViewTesting, "Hello there", Toast.LENGTH_SHORT).show()
                }),
                MaterialItem(HubType.STAR, "Cartoons", "Cartoon", R.drawable.cartoon_cover, null, {}, "Click Me", {
                    Toast.makeText(this@ViewTesting, "Hello there", Toast.LENGTH_SHORT).show()
                }),
                MaterialItem(HubType.STAR, "Star", "Stars", R.drawable.star_off, "Hello", {
                    Toast.makeText(this@ViewTesting, "Hello there", Toast.LENGTH_SHORT).show()
                }, "Click Me", {
                    Toast.makeText(this@ViewTesting, "Hello there", Toast.LENGTH_SHORT).show()
                }))

        material_test.adapter = MaterialAdapter(mList, this) {
            when (this) {
                HubType.STAR -> {
                    Toast.makeText(this@ViewTesting, "Hello there", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    enum class HubType {
        STAR
    }

    data class MaterialItem(val hubType: HubType, val title: String, val detail: String, val image: Any?,
                            val buttonTextOne: String? = null, val actionOne: (() -> Unit)? = null,
                            val buttonTextTwo: String? = null, val actionTwo: (() -> Unit)? = null,
                            @DrawableRes val bgImage: Int? = null)

    class MaterialAdapter(
            list: ArrayList<MaterialItem>,
            private val context: Context,
            private val onPress: HubType.() -> Unit
    ) : DragSwipeAdapter<MaterialItem, ViewHolder>(list) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            list[position].apply {
                holder.title.text = title
                holder.detail.text = detail
                when (image) {
                    is Uri? -> Picasso.get().load(image).into(holder.image)
                    is File -> Picasso.get().load(image).into(holder.image)
                    is Int -> Picasso.get().load(image).into(holder.image)
                    is String? -> Picasso.get().load(image).into(holder.image)
                }
                holder.actionOne.visibility = if (buttonTextOne.isNullOrBlank()) View.GONE else View.VISIBLE
                holder.actionTwo.visibility = if (buttonTextTwo.isNullOrBlank()) View.GONE else View.VISIBLE
                holder.actionOne.text = buttonTextOne
                holder.actionTwo.text = buttonTextTwo
                holder.actionOne.setOnClickListener { actionOne?.invoke() }
                holder.actionTwo.setOnClickListener { actionTwo?.invoke() }
                holder.itemView.setOnClickListener { hubType.onPress() }
                bgImage?.let {
                    holder.itemView.setBackgroundResource(it)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                    LayoutInflater.from(context).inflate(
                            R.layout.material_card_hub_item,
                            parent,
                            false
                    )
            )
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.material_Image!!
        val title: TextView = view.material_title!!
        val detail: TextView = view.material_detail!!
        val actionOne: MaterialButton = view.action_one!!
        val actionTwo: MaterialButton = view.action_two!!
    }

}
