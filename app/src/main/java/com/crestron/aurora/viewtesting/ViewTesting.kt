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

        data class Contact(
                val name: String,
                val phone: String
        )

        val contacts = listOf(
                Contact("Alex", "(751) 053-9233"),
                Contact("Bob", "(520) 395-8395"),
                Contact("Carl", "(203) 595-2852"),
                Contact("Dan", "(182) 735-4068")
        )

        val contacts2 = listOf(
                Contact("Ember", "(582) 015-3298"),
                Contact("Frank", "(205) 196-7230"),
                Contact("Gordon", "(293) 582-3985"),
                Contact("Harry", "(492) 359-3953")
        )

        /*with(vs_test) {
            insertItemToEnd("Contacts", R.layout.text_layout) { title ->
                itemView.link_list.text = title
            }

            insertNoDataItemToEnd(R.layout.material_drawer_item_divider)

            insertItemsToEnd(contacts, R.layout.material_card_hub_item) { contact ->
                itemView.material_title.text = contact.name
                itemView.material_detail.text = contact.phone
            }

            insertItemsToEnd(contacts2, R.layout.rss_layout_item) { contacting ->
                itemView.description.text = contacting.name
                itemView.item_feed.text = contacting.phone
            }
        }

        test_rv.adapter = DynamicAdapter(this).apply {
            contacts.addItems(R.layout.material_card_hub_item) {
                material_title.text = it.name
                material_detail.text = it.phone
            }

            addItems(R.layout.rss_layout_item, contacts2) {
                description.text = it.name
                item_feed.text = it.phone
            }
        }*/

    }

}
