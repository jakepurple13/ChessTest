package com.crestron.aurora.cardgames.solitaire

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crestron.aurora.R
import crestron.com.deckofcards.Card
import kotlinx.android.synthetic.main.card_item.view.*

class CardAdapter(private val items : ArrayList<Card>, val context: Context, private val location: Int, private val action: SolitaireActivity.AdapterPress) : RecyclerView.Adapter<ViewHolder>() {

    private var lastView: View? = null

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_item, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.cardType.text = "${items[position]}"
        val id = if(items.isEmpty()) {
            Card.ClearCard.getImage(context)
        } else {
            items[position].getImage(context)
        }
        holder.cardType.setImageResource(id)
        holder.cardType.contentDescription = items[position].toString()
        // = "${items[position]}"
        holder.cardType.setOnClickListener {
            action.action(position, items[position], location)
        }

        /*holder.cardType.setOnLongClickListener {
            Loged.wtf("$items")
            true
        }*/

        if(position==items.size-1) {
            lastView = holder.cardType
        }

    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    //val cardType = view.card_info!!
    val cardType = view.card_info_cards!!
}