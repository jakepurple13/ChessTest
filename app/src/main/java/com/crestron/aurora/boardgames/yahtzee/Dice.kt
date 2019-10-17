package com.crestron.aurora.boardgames.yahtzee

import com.crestron.aurora.R

class Dice(val num: Int) {
    val image = when (num) {
        1 -> R.drawable.one_dice
        2 -> R.drawable.two_dice
        3 -> R.drawable.three_dice
        4 -> R.drawable.four_dice
        5 -> R.drawable.five_dice
        6 -> R.drawable.six_dice
        else -> 0
    }
}