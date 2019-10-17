package com.crestron.aurora.boardgames.yahtzee

import java.util.*
import kotlin.math.max

class YahtzeeScores {
    private var gotYahtzee = false
    private var threeOfKind = 0
        set(value) {
            field = value
            largeTotal += value
        }
    private var fourOfKind = 0
        set(value) {
            field = value
            largeTotal += value
        }
    private var fullHouse = 0
        set(value) {
            field = value
            largeTotal += value
        }
    private var smallStraight = 0
        set(value) {
            field = value
            largeTotal += value
        }
    private var largeStraight = 0
        set(value) {
            field = value
            largeTotal += value
        }
    private var yahtzee = 0
        set(value) {
            largeTotal += value - field
            field = value
        }
    private var chance = 0
        set(value) {
            field = value
            largeTotal += value
        }
    private var ones = 0
        set(value) {
            field = value
            smallTotal += value
        }
    private var twos = 0
        set(value) {
            field = value
            smallTotal += value
        }
    private var threes = 0
        set(value) {
            field = value
            smallTotal += value
        }
    private var fours = 0
        set(value) {
            field = value
            smallTotal += value
        }
    private var fives = 0
        set(value) {
            field = value
            smallTotal += value
        }
    private var sixes = 0
        set(value) {
            field = value
            smallTotal += value
        }
    private var over = false
    var smallTotal = 0
        set(value) {
            field = if (!over && value >= 63) {
                over = true
                value + 35
            } else {
                value
            }
            total = largeTotal + smallTotal
        }
    var largeTotal = 0
        set(value) {
            field = value
            total = largeTotal + smallTotal
        }
    var total = 0

    private fun getSmallNum(dice: Collection<Dice>, num: Int): Int = dice.filter { it.num == num }.sumBy { it.num }

    fun getOnes(dice: Collection<Dice>): Int = getSmallNum(dice, 1).apply { ones = this }
    fun getTwos(dice: Collection<Dice>): Int = getSmallNum(dice, 2).apply { twos = this }
    fun getThrees(dice: Collection<Dice>): Int = getSmallNum(dice, 3).apply { threes = this }
    fun getFours(dice: Collection<Dice>): Int = getSmallNum(dice, 4).apply { fours = this }
    fun getFives(dice: Collection<Dice>): Int = getSmallNum(dice, 5).apply { fives = this }
    fun getSixes(dice: Collection<Dice>): Int = getSmallNum(dice, 6).apply { sixes = this }

    fun canGetThreeKind(dice: Collection<Dice>): Boolean {
        val values = dice.groupingBy { it.num }.eachCount().values
        return 3 in values || 4 in values || 5 in values
    }

    fun getThreeOfAKind(dice: Collection<Dice>): Int = if (canGetThreeKind(dice)) {
        dice.sumBy { it.num }
    } else {
        0
    }.apply { threeOfKind = this }

    fun canGetFourKind(dice: Collection<Dice>): Boolean {
        val values = dice.groupingBy { it.num }.eachCount().values
        return 4 in values || 5 in values
    }

    fun getFourOfAKind(dice: Collection<Dice>): Int = if (canGetFourKind(dice)) {
        dice.sumBy { it.num }
    } else {
        0
    }.apply { fourOfKind = this }

    fun canGetYahtzee(dice: Collection<Dice>): Boolean = 5 in dice.groupingBy { it.num }.eachCount().values

    fun getYahtzee(dice: Collection<Dice>): Int = (if (canGetYahtzee(dice)) if (gotYahtzee) 100 else 50 else 0).apply {
        gotYahtzee = true
        yahtzee += this
    }

    fun canGetFullHouse(dice: Collection<Dice>): Boolean {
        val values = dice.groupingBy { it.num }.eachCount().values
        return 3 in values && 2 in values
    }

    fun getFullHouse(dice: Collection<Dice>): Int = (if (canGetFullHouse(dice)) 25 else 0).apply { fullHouse = this }

    fun canGetLargeStraight(dice: Collection<Dice>): Boolean {
        val filteredDice = dice.sortedBy { it.num }
        return longestSequence(filteredDice.toTypedArray()) == 4
    }

    fun getLargeStraight(dice: Collection<Dice>): Int = (if (canGetLargeStraight(dice)) 40 else 0).apply { largeStraight = this }

    fun canGetSmallStraight(dice: Collection<Dice>): Boolean {
        val filteredDice = dice.sortedBy { it.num }
        return longestSequence(filteredDice.toTypedArray()) in 3..4
    }

    fun getSmallStraight(dice: Collection<Dice>): Int = (if (canGetSmallStraight(dice)) 30 else 0).apply { smallStraight = this }

    fun getChance(dice: Collection<Dice>): Int {
        return dice.sumBy { it.num }.apply {
            chance = this
        }
    }

    private fun longestSequence(a: Array<Dice>): Int {
        Arrays.sort(a, compareBy { it.num })
        var longest = 0
        var sequence = 0
        for (i in 1 until a.size) {
            when (a[i].num - a[i - 1].num) {
                0 -> {/*ignore duplicates*/
                }
                1 -> sequence += 1
                else -> if (sequence > longest) {
                    longest = sequence
                    sequence = 0
                }
            }
        }
        return max(longest, sequence)
    }

}