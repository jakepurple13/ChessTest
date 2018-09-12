package com.crestron.aurora.boardgames.chess

import com.crestron.aurora.Loged

abstract class Piece(val color: Boolean, loc: Location) : Comparable<Piece> {

    override fun compareTo(other: Piece): Int {
        return if (color == other.color) {
            0
        } else {
            -1
        }
    }

    fun equals(o: Piece): Boolean {
        // TODO Auto-generated method stub
        return color == o.color
    }

    var icon: Char = ' '
    lateinit var board: Board
    var location: Location = loc
    var moved = false
    var pieceValue: String = when (this) {
        is King -> "K"
        is Queen -> "Q"
        is Rook -> "R"
        is Bishop -> "B"
        is Knight -> "N"
        is Pawn -> ""
        else -> ""
    }
    var value = when (this) {
        is King -> 10
        is Queen -> 9
        is Rook -> 5
        is Bishop -> 3
        is Knight -> 3
        is Pawn -> 1
        else -> 0
    }

    abstract fun checkMove(loc: Location): Boolean

    fun lineEmpty(rowto: Int, columnto: Int): Boolean {
        var offset: Int

        val (fromRow, fromCol) = location

        if (fromRow != rowto) {
            offset = if (fromRow < rowto) {
                1
            } else {
                -1
            }

            var x = fromRow + offset
            while (x != rowto) {
                //Go from currentRow to newRow, and check every space
                if (!board.isEmpty(Location(x, fromCol))) {
                    return false
                }
                x += offset
            }
        }

        if (fromCol != columnto) {
            offset = if (fromCol < columnto) {
                1
            } else {
                -1
            }

            var x = fromCol + offset
            while (x != columnto) {
                //Go from currentCol to newCol, and check every space
                if (!board.isEmpty(Location(fromRow, x))) {
                    return false
                }
                x += offset
            }
        }

        return true
    }

    fun diagnolEmpty(rowto: Int, columnto: Int): Boolean {

        val (fromRow, fromCol) = location

        if (fromRow == rowto || fromCol == columnto) {
            //Did not move diagonally
            return false
        }

        if (Math.abs(rowto - fromRow) != Math.abs(columnto - fromCol)) {
            return false
        }

        val rowOffset: Int
        val colOffset: Int

        rowOffset = if (fromRow < rowto) {
            1
        } else {
            -1
        }

        colOffset = if (fromCol < columnto) {
            1
        } else {
            -1
        }

        var y = fromCol + colOffset

        var x = fromRow + rowOffset
        while (x != rowto) {

            if (!board.isEmpty(Location(x, y))) {
                return false
            }

            y += colOffset
            x += rowOffset
        }
        return true
    }

    fun isPromotional(): Boolean {
        val (_, fromCol) = location
        if (this is Pawn) {
            if (board.getLocation(0, fromCol) is Pawn) {
                return true
            } else if (board.getLocation(7, fromCol) is Pawn) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "$icon is at $location"
    }

}

class EmptySpace(color: Boolean, loc: Location) : Piece(color, loc) {
    override fun checkMove(loc: Location): Boolean {
        Loged.wtf(loc.toString())
        return false
    }
}

class Pawn(color: Boolean, loc: Location) : Piece(color, loc) {

    var ep = false

    init {
        icon = if (color) {
            '\u2659'
        } else {
            '\u265F'
        }
        location = loc
    }

    override fun checkMove(loc: Location): Boolean {
        var moves = false
        val (fromRow, fromCol) = location
        val (rowto, columnto) = loc
        //moving
        if (board.isEmpty(loc)) {
            //moving up two
            if (!moved && Math.abs(fromRow - rowto) == 2 && fromCol == columnto) {
                moves = true
                ep = true
                //moving up one
            } else if ((fromRow + 1 == rowto || fromRow - 1 == rowto) && fromCol == columnto) {
                moves = true
                ep = false
            }
            //capturing
        } else if (!board.isEmpty(loc)) {
            if (this != board.getLocation(rowto, columnto)) {
                if ((fromRow + 1 == rowto || fromCol + 1 == columnto) && (fromRow - 1 == rowto || fromCol - 1 == columnto)) {
                    moves = true
                    ep = false
                } else if ((fromRow + 1 == rowto || fromCol - 1 == columnto) && (fromRow - 1 == rowto || fromCol + 1 == columnto)) {
                    moves = true
                    ep = false
                }
            }
        }
        isPromotional()
        //if(moves) moved = true
        return moves
    }
}

class Rook(color: Boolean, loc: Location) : Piece(color, loc) {

    init {
        icon = if (color) {
            '\u2656'
        } else {
            '\u265C'
        }
        location = loc
    }

    override fun checkMove(loc: Location): Boolean {

        val (letter, num1) = loc

        var moves = false
        //moving
        if (board.isEmpty(loc) && lineEmpty(letter, num1)) {
            moves = true
            //capturing
        } else if (!board.isEmpty(loc) && lineEmpty(letter, num1)) {
            if (!this.equals(board.getLocation(letter, num1))) {
                moves = true
            }
            //castling
        }
        if (moves) moved = true
        return moves
    }
}

class Knight(color: Boolean, loc: Location) : Piece(color, loc) {

    init {
        icon = if (color) {
            '\u2658'
        } else {
            '\u265E'
        }
        location = loc
    }

    override fun checkMove(loc: Location): Boolean {
        var moves = false
        val (fromRow, fromCol) = location
        val (rowto, columnto) = loc

        if (Math.abs(rowto - fromRow) == 2 && Math.abs(columnto - fromCol) == 1) {
            if (board.isEmpty(loc)) {
                moves = true
            } else if (!this.equals(board.getLocation(rowto, columnto))) {
                moves = true
            }
        }

        if (Math.abs(rowto - fromRow) == 1 && Math.abs(columnto - fromCol) == 2) {
            if (board.isEmpty(loc)) {
                moves = true
            } else if (!this.equals((board.getLocation(rowto, columnto)))) {
                moves = true
            }
        }
        if (moves) moved = true
        return moves
    }
}

class Bishop(color: Boolean, loc: Location) : Piece(color, loc) {

    init {
        icon = if (color) {
            '\u2657'
        } else {
            '\u265D'
        }
        location = loc
    }

    override fun checkMove(loc: Location): Boolean {
        var moves = false
        val (rowto, columnto) = loc
        if (board.isEmpty(loc) && diagnolEmpty(rowto, columnto)) {
            moves = true
            //capturing
        } else if (!board.isEmpty(loc) && diagnolEmpty(rowto, columnto)) {
            if (!this.equals((board.getLocation(rowto, columnto)))) {
                moves = true
            }
        }
        if (moves) moved = true
        return moves
    }
}

class Queen(color: Boolean, loc: Location) : Piece(color, loc) {

    init {
        icon = if (color) {
            '\u2655'
        } else {
            '\u265B'
        }
        location = loc
    }

    override fun checkMove(loc: Location): Boolean {
        var moves = false
        val (rowto, columnto) = loc
        if (board.isEmpty(loc) && (diagnolEmpty(rowto, columnto) || lineEmpty(rowto, columnto))) {
            moves = true
            //capturing
        } else if (!board.isEmpty(loc) && (diagnolEmpty(rowto, columnto) || lineEmpty(rowto, columnto))) {
            if (!this.equals((board.getLocation(rowto, columnto)))) {
                moves = true
            }
        }
        if (moves) moved = true
        return moves
    }
}

class King(color: Boolean, loc: Location) : Piece(color, loc) {

    var hasMoved = false

    init {
        icon = if (color) {
            '\u2654'
        } else {
            '\u265A'
        }
        location = loc
    }

    override fun checkMove(loc: Location): Boolean {
        val (rowto, columnto) = loc
        val (fromRow, fromCol) = location
        var moves = false
        //castling	
        if (!hasMoved && columnto - fromCol == 2 && fromRow == rowto) {
            //kingside
            if (board.isEmpty(Location(rowto, fromCol + 1)) || board.isEmpty(Location(rowto, fromCol + 2))) {
                moves = true
                board.getLocation(rowto, fromCol + 3).moved = true
            }
        } else if (!hasMoved && fromCol - columnto == 2 && fromRow == rowto) {
            //queenside
            if (board.isEmpty(Location(rowto, fromCol - 1)) ||
                    board.isEmpty(Location(rowto, fromCol - 2)) ||
                    board.isEmpty(Location(rowto, fromCol - 3))) {
                moves = true
                board.getLocation(rowto, fromCol - 4).moved = true
            }
            //moving
        } else if (board.isEmpty(loc)) {
            if (fromRow + 1 == rowto || fromCol + 1 == columnto || fromRow - 1 == rowto || fromCol - 1 == columnto) {
                moves = true
            }
            //capturing
        } else if (!board.isEmpty(loc)) {
            if (!this.equals(board.getLocation(rowto, columnto))) {
                if (fromRow + 1 == rowto || fromCol + 1 == columnto || fromRow - 1 == rowto || fromCol - 1 == columnto) {
                    moves = true
                }
            }
        }
        if (moves) moved = true
        return moves
    }
}

class Location(var num: Int) {

    var letter: String = ""
    var numRepLetter = 0

    constructor(letters: String, num: Int) : this(num) {
        letter = letters
        numRepLetter = letter[0] - 'A'
    }

    constructor(let: Int = 0, num: Int) : this(num) {
        numRepLetter = let
        letter = Character.toString(('A'.toInt() + numRepLetter).toChar())
    }

    override fun toString(): String {
        return "$letter$num"
    }

    operator fun component1(): Int {
        return numRepLetter
    }

    operator fun component2(): Int {
        return num
    }

    companion object Locate {
        fun toLocation(s: String): Location {
            return Location(Character.getNumericValue(s[0]), Character.getNumericValue(s[1]))
        }

        fun toLocation(s: String, b: Boolean): Location {
            return Location(Character.getNumericValue(s[0]) - Character.getNumericValue('A'), Character.getNumericValue(s[1]))
        }

        fun toLocation(num: Int, n: Int): Location {
            return Location(num, n)
        }
    }
}