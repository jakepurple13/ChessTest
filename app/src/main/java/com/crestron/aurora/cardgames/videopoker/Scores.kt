package com.crestron.aurora.cardgames.videopoker

import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Hand

class Scores {

    val values: String
        get() {
            var values = ""

            var value = 1.0
            var next: Int

            for (i in 9 downTo 1) {
                when (i) {
                    9 -> {
                        values += "Royal Flush:    |"
                        value = 250 / 9.0
                    }
                    8 -> {
                        values += "Straight Flush: |"
                        value = 6.25
                    }
                    7 -> {
                        values += "4 of a Kind:    |"
                        value = 25 / 7.0
                    }
                    6 -> {
                        values += "Full House:     |"
                        value = 9 / 6.0
                    }
                    5 -> {
                        values += "Flush:          |"
                        value = 6 / 5.0
                    }
                    4 -> {
                        values += "Straight:       |"
                        value = 4 / 4.0
                    }
                    3 -> {
                        values += "3 of a Kind:    |"
                        value = 3 / 3.0
                    }
                    2 -> {
                        values += "Two Pair:       |"
                        value = 2 / 2.0
                    }
                    1 -> {
                        values += "Pair:           |"
                        value = 1 / 1.0
                    }
                    else -> {
                    }
                }
                for (j in 1..5) {
                    next = (i.toDouble() * j.toDouble() * value).toInt()
                    values += next.toString() + "|"
                }
                values += "\n"
            }

            return values
        }

    fun htmlValues(typeHand: String, betAmount: Int): String {

        var value = 1.0
        var next: Int

        val num = when (typeHand) {
            "Royal Flush" -> 9
            "Straight Flush" -> 8
            "Four of a Kind" -> 7
            "Full House" -> 6
            "Flush" -> 5
            "Straight" -> 4
            "Three of a Kind" -> 3
            "Two Pair" -> 2
            "Pair" -> 1
            else -> 0
        }

        val seq = sequence {
            while(true)
                yield("\u2007")
        }

        val handList = arrayOfNulls<String>(10)

        for (i in 9 downTo 0) {
            var s = ""
            if(num==i && i!=0)
                s += "<font color=\"red\">"
            when (i) {
                9 -> {
                    s += "Royal Flush:    |"
                    value = 250 / 9.0
                }
                8 -> {
                    s += "Straight Flush: |"
                    value = 50 / 8.0
                }
                7 -> {
                    s += "4 of a Kind:    |"
                    value = 25 / 7.0
                }
                6 -> {
                    s += "Full House:     |"
                    value = 9 / 6.0
                }
                5 -> {
                    s += "Flush:          |"
                    value = 6 / 5.0
                }
                4 -> {
                    s += "Straight:       |"
                    value = 4 / 4.0
                }
                3 -> {
                    s += "3 of a Kind:    |"
                    value = 3 / 3.0
                }
                2 -> {
                    s += "Two Pair:       |"
                    value = 2 / 2.0
                }
                1 -> {
                    s += "Pair:           |"
                    value = 1 / 1.0
                }
                else -> {
                    s+=""
                }
            }
            for (j in 1..5) {
                if(i!=0) {
                    next = (i.toDouble() * j.toDouble() * value).toInt()
                    s += if(j==betAmount)
                        "</font><font color =\"green\">" +
                                "${seq.take(4-next.toString().length).joinToString("")}$next</font>" +
                                "${if(i==num) "<font color=\"red\">" else ""}|"
                    else
                    //"${String.format("%04d", next)}|"
                        "${seq.take(4-next.toString().length).joinToString("")}$next|"
                    if (i == num && j == 5) {
                        s += "</font>"
                    }
                }
            }
            handList[i] = s
        }

        handList.reverse()

        var stringToShow = "<html>"

        for (check in handList) {
            stringToShow+="<p>$check</p>"
        }

        stringToShow+="</html>"

        return stringToShow
    }

    fun htmlValues(betAmount: Int): String {

        var value = 1.0
        var next: Int

        val seq = sequence {
            while(true)
                yield("\u2007")
        }

        val handList = arrayOfNulls<String>(10)

        for (i in 9 downTo 0) {
            var s = ""
            when (i) {
                9 -> {
                    s += "Royal Flush:    |"
                    value = 250 / 9.0
                }
                8 -> {
                    s += "Straight Flush: |"
                    value = 50 / 8.0
                }
                7 -> {
                    s += "4 of a Kind:    |"
                    value = 25 / 7.0
                }
                6 -> {
                    s += "Full House:     |"
                    value = 9 / 6.0
                }
                5 -> {
                    s += "Flush:          |"
                    value = 6 / 5.0
                }
                4 -> {
                    s += "Straight:       |"
                    value = 4 / 4.0
                }
                3 -> {
                    s += "3 of a Kind:    |"
                    value = 3 / 3.0
                }
                2 -> {
                    s += "Two Pair:       |"
                    value = 2 / 2.0
                }
                1 -> {
                    s += "Pair:           |"
                    value = 1 / 1.0
                }
                else -> {
                    s+=""
                }
            }
            for (j in 1..5) {
                if(i!=0) {
                    next = (i.toDouble() * j.toDouble() * value).toInt()
                    s += if(j==betAmount)
                        "</font><font color =\"green\">" +
                                "${seq.take(4-next.toString().length).joinToString("")}$next</font>|"
                    else
                    //"${String.format("%04d", next)}|"
                        "${seq.take(4-next.toString().length).joinToString("")}$next|"
                    if (j == 5) {
                        s += "</font>"
                    }
                }
            }
            handList[i] = s
        }

        handList.reverse()

        var stringToShow = "<html>"

        for (check in handList) {
            stringToShow+="<p>$check</p>"
        }

        stringToShow+="</html>"

        return stringToShow
    }

    var jacksOrBetter = false

    private fun pair(hand: Hand): Boolean {
        val h = hand.hand.sortedBy { it.value }//h.sortHandByValue()
        var acceptable = false
        var count = 0
        for (i in 1 until h.size) {
            //if (h.getCard(i).compareTo(h.getCard(i - 1)) == 0) {
            val valueMin = if (jacksOrBetter) 11 else 1
            if (h.getCard(i).compareTo(h.getCard(i - 1)) == 0 && h.getCard(i).value > valueMin || h.getCard(i).value == 1) {
                count++
            }

        }

        if (count == 1) {
            acceptable = true
        }

        return acceptable
    }

    private fun twoPair(hand: Hand): Boolean {
        val h = hand.hand.sortedBy { it.value }//h.sortHandByValue()
        var count = 1
        var found = false
        var found1 = false
        var i = 1
        while (i < h.size) {
            if (h.getCard(i).compareTo(h.getCard(i - 1)) == 0) {
                count++
                i++
            }
            if (count == 2 && found1) {
                found = true
                count = 1
            } else if (count == 2) {
                found1 = true
                count = 1
            }
            i++


        }

        if (count == 2) {
            found1 = true
        } else if (count == 2 && found1) {
            found = true
        }

        return found && found1

    }

    private fun threeOfAKind(hand: Hand): Boolean {
        val h = hand.hand.sortedBy { it.value }//h.sortHandByValue()
        var acceptable = false
        var count = 1
        var hold = false
        for (i in 1 until h.size) {

            if (h.getCard(i).compareTo(h.getCard(i - 1)) == 0) {
                count++
                hold = true
            } else if (hold) {
                break
            }

        }

        if (count == 3) {
            acceptable = true
        }

        return acceptable
    }

    private fun fourOfAKind(hand: Hand): Boolean {
        val h = hand.hand.sortedBy { it.value }//h.sortHandByValue()
        var acceptable = false
        var count = 0
        val numberCount = h.getCard(3).value
        for (i in 0 until h.size) {

            if (h.getCard(i).value == numberCount) {
                count++
            }
        }
        if (count == 4) {
            acceptable = true
        }

        return acceptable
    }

    private fun fullHouse(hand: Hand): Boolean {

        val h = hand.hand.sortedBy { it.value }//h.sortHandByValue()
        var count = 1
        var found = false
        var found1 = false
        for (i in 1 until h.size) {
            if (h.getCard(i).compareTo(h.getCard(i - 1)) == 0) {
                count++
            } else {
                if (count == 3) {
                    found1 = true
                } else if (count == 2) {
                    found = true
                }
                count = 1
            }

        }

        if (count == 3) {
            found1 = true
        } else if (count == 2) {
            found = true
        }

        return found && found1

    }

    private fun straight(hand: Hand): Boolean {

        val h = hand.hand.sortedBy { it.value }//h.sortHandByValue()
        var count = 0
        var value: Int
        for (i in 0 until h.size - 1) {
            value = h.getCard(i).value
            if (value == 1) {
                if (h.getCard(i + 1).value == 2) {
                    value = 1
                } else if (h.getCard(i + 1).value == 10) {
                    value = 9
                }
            }
            if (value + 1 == h.getCard(i + 1).value) {
                count++
            }
        }

        return count == 4
    }

    private fun flush(hand: Hand): Boolean {

        val h = hand.hand.sortedBy { it.value }//h.sortHandByValue()
        for (i in 1 until h.size) {
            if (!h.getCard(i).compareSuit(h.getCard(i - 1))) {
                return false
            }
        }
        return true
    }

    private fun straightFlush(h: Hand): Boolean {
        return straight(h) && flush(h)
    }

    private fun royalFlush(hand: Hand): Boolean {

        val h = hand.hand.sortedBy { it.value }//h.sortHandByValue()
        if (h.getCard(1).value == 10) {
            if (h.getCard(2).value == 11) {
                if (h.getCard(3).value == 12) {
                    if (h.getCard(4).value == 13) {
                        if (h.getCard(0).value == 1) {
                            if (straight(hand) && flush(hand)) {
                                return true
                            }
                        }
                    }
                }
            }
        }

        return false
    }


    fun winCheck(h: Hand, bet: Int): Int {
        return when {
            royalFlush(h) -> {
                println("Royal Flush + $h")
                getWinning(9, bet)
            }
            straightFlush(h) -> {
                println("Straight Flush + $h")
                getWinning(8, bet)
            }
            fourOfAKind(h) -> {
                println("Four of a Kind + $h")
                getWinning(7, bet)
            }
            fullHouse(h) -> {
                println("Full House + $h")
                getWinning(6, bet)
            }
            flush(h) -> {
                println("Flush + $h")
                getWinning(5, bet)
            }
            straight(h) -> {
                println("Straight + $h")
                getWinning(4, bet)
            }
            threeOfAKind(h) -> {
                println("Three of a Kind + $h")
                getWinning(3, bet)
            }
            twoPair(h) -> {
                println("Two Pair + $h")
                getWinning(2, bet)
            }
            pair(h) -> {
                println("Pair + $h")
                getWinning(1, bet)
            }
            else -> -bet
        }
    }

    fun getWinningHand(h: Hand): String {
        return when {
            royalFlush(h) -> {
                "Royal Flush"
            }
            straightFlush(h) -> {
                "Straight Flush"
            }
            fourOfAKind(h) -> {
                "Four of a Kind"
            }
            fullHouse(h) -> {
                "Full House"
            }
            flush(h) -> {
                "Flush"
            }
            straight(h) -> {
                "Straight"
            }
            threeOfAKind(h) -> {
                "Three of a Kind"
            }
            twoPair(h) -> {
                "Two Pair"
            }
            pair(h) -> {
                "Pair"
            }
            else -> "Nothing"
        }
    }

    private fun getWinning(rank: Int, bet: Int): Int {
        var values = ""

        var value = 1.0

        when (rank) {
            9 -> {
                values += "Royal Flush:    |"
                value = 250.0
            }
            8 -> {
                values += "Straight Flush: |"
                value = 50.0
            }
            7 -> {
                values += "4 of a Kind:    |"
                value = 25.0
            }
            6 -> {
                values += "Full House:     |"
                value = 9.0
            }
            5 -> {
                values += "Flush:          |"
                value = 6.0
            }
            4 -> {
                values += "Straight:       |"
                value = 4.0
            }
            3 -> {
                values += "3 of a Kind:    |"
                value = 3.0
            }
            2 -> {
                values += "Two Pair:       |"
                value = 2.0
            }
            1 -> {
                values += "Pair:           |"
                value = 1.0
            }
            else -> println(values)
        }
        return (value * bet).toInt()
    }

}

private fun List<Card>.getCard(i: Int): Card {
    return this[i]
}

