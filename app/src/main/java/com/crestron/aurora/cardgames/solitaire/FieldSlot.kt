package com.crestron.aurora.cardgames.solitaire

import android.content.Context
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Suit

class FieldSlot(private val context: Context, numOfCards: Int, d: Deck) {

    private val faceDownList: ArrayList<Card> = arrayListOf()
    var list: ArrayList<Card> = arrayListOf()

    init {
        for (i in 0 until numOfCards) {
            faceDownList.add(d.draw())
        }
        list.add(d.draw())
    }

    fun checkToAdd(c: List<Card>): Boolean {
        try {
            if ((list.last().color != c[0].color && list.last().value - 1 == c[0].value)) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if ((listSize() == 0 && c[0].value == 13)) {
                return true
            }
        }
        return false
    }

    fun checkToAdd(c: Card): Boolean {
        try {
            if ((list.last().color != c.color && list.last().value - 1 == c.value)) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (listSize() == 0 && c.value == 13) {
                return true
            }
        }
        return false
    }

    fun addCards(c: List<Card>) {
        list.addAll(c)
    }

    fun addCard(c: Card) {
        list.add(c)
    }

    @Throws(IndexOutOfBoundsException::class)
    fun getCard(num: Int): Card {
        return list[num]
    }

    operator fun get(num: Int): Card {
        return list[num]
    }

    @Throws(IndexOutOfBoundsException::class)
    fun getCards(num: Int): List<Card> {
        return list.subList(num, list.size)
    }

    private fun removeCard(num: Int): Card {
        return list.removeAt(num)
    }

    fun removeCard(): Card {
        return list.removeAt(listSize() - 1)
    }

    @Throws(NoSuchElementException::class)
    fun lastCard(): Card {
        return list.last()
    }

    fun removeCards(num: Int): List<Card> {
        val cardList: ArrayList<Card> = arrayListOf()
        var i = num
        while (num < list.size) {
            cardList.add(removeCard(i))
            i--
            i++
        }
        return cardList
    }

    fun getImage(): Int {
        return try {
            list.last().getImage(context)
        } catch (e: NoSuchElementException) {
            Card(Suit.SPADES, 15).getImage(context)
        }
    }

    fun flipFaceDownCard(): Int {
        if (list.size == 0 && faceDownList.size > 0) {
            list.add(faceDownList.removeAt(faceDownList.size - 1))
            return 5
        }
        return 0
    }

    fun canFlipFaceDownCard(): Boolean {
        if (list.size == 0 && faceDownList.size > 0) {
            return true
        }
        return false
    }

    fun faceDownSize(): Int {
        return faceDownList.size
    }

    fun listSize(): Int {
        return list.size
    }

    override fun toString(): String {
        var s = ""

        for (i in list) {
            s += "$i\n"
        }

        return s
    }

}