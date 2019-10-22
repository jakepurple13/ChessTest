package com.crestron.aurora

import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import kotlinx.coroutines.runBlocking
import org.apache.tools.ant.util.DateUtils
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis

@Suppress("SameParameterValue")
class TestUnitThree {

    @Before
    fun setUp() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
        Loged.WITH_THREAD_NAME = true
    }

    @Test
    fun sortTesting() = runBlocking {

        val endDeck = Deck(shuffler = true).getDeck().sorted().map { it.value }
        prettyLog(endDeck)

        sortThings("Default") {
            sort()
        }
        sortThings("Bubble") {
            BubbleSort().perform(this)
        }
        sortThings("Insertion") {
            InsertionSort().perform(this)
        }
        sortThings("Merge") {
            MergeSort<Card>().sort(this)
        }
        sortThings("Quick") {
            QuickSort().perform(this)
        }
        sortThings("Selection") {
            SelectionSort().perform(this)
        }
        sortThings("Shell") {
            ShellSort().perform(this)
        }

    }

    private fun sortThings(type: String, sortMethod: MutableList<Card>.() -> Unit) {
        var s = "$type\n"
        val cNum = measureTimeMillis {
            val d = Deck(shuffler = true, numberOfDecks = 50).getDeck()
            s += d.map { it.value }
            d.sortMethod()
            s += "\n" + d.map { it.value }
        }
        s += "\n$cNum milliseconds\n${cNum.toElapsed()}"
        prettyLog(s)
    }

    @Test
    fun kotlinTest() {
        val data = intArrayOf(2147483647, 2147483647, 2147483647)
        prettyLog(indexOfMax(data))
    }

    private fun indexOfMax(a: IntArray): Int? {
        return try {
            a.lastIndexOf(a.max()!!)
        } catch(e: NullPointerException) {
            null
        }
    }

    @Test
    fun searchTesting() = runBlocking {
        val c = Card.RandomCard
        prettyLog(c)
        val d = Deck().getDeck()

        val cNum = binarySearch(c, d)
        prettyLog("$cNum milliseconds")
        prettyLog(cNum.toElapsed())

        val cNum2 = linearSearch(c, d)
        prettyLog("$cNum2 milliseconds")
        prettyLog(cNum2.toElapsed())
    }

    private fun linearSearch(c: Card, d: ArrayList<Card>) = measureTimeMillis {
        var c1: Card? = null
        for (i in d) {
            if (i == c) {
                c1 = i
                break
            }
        }
        prettyLog(c1)
    }

    private fun binarySearch(c: Card, d: ArrayList<Card>) = measureTimeMillis {
        val c1 = d.binarySearch(c)
        prettyLog(c1)
    }

    private fun Long.toElapsed(): String = DateUtils.formatElapsedTime(this)

    private fun prettyLog(msg: Any?) {
        //the main message to be logged
        var logged = msg.toString()
        //the arrow for the stack trace
        val arrow = "${9552.toChar()}${9655.toChar()}\t"
        //the stack trace
        val stackTraceElement = Thread.currentThread().stackTrace

        val elements = listOf(*stackTraceElement)
        val wanted = elements.filter { it.className.contains(Loged.FILTER_BY_CLASS_NAME) && !it.methodName.contains("prettyLog") }

        var loc = "\n"

        for (i in wanted.indices.reversed()) {
            val fullClassName = wanted[i].className
            //get the method name
            val methodName = wanted[i].methodName
            //get the file name
            val fileName = wanted[i].fileName
            //get the line number
            val lineNumber = wanted[i].lineNumber
            //add this to location in a format where we can click on the number in the console
            loc += "$fullClassName.$methodName($fileName:$lineNumber)"

            if (wanted.size > 1 && i - 1 >= 0) {
                val typeOfArrow: Char =
                        if (i - 1 > 0)
                            9568.toChar() //middle arrow
                        else
                            9562.toChar() //ending arrow
                loc += "\n\t$typeOfArrow$arrow"
            }
        }

        logged += loc

        println(logged + "\n")
    }

}

fun <T> MutableList<T>.exch(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

@Retention(AnnotationRetention.SOURCE)
annotation class ComparisonSort

@Retention(AnnotationRetention.SOURCE)
annotation class StableSort

@Retention(AnnotationRetention.SOURCE)
annotation class UnstableSort

abstract class AbstractSortStrategy {
    abstract fun <T : Comparable<T>> perform(arr: MutableList<T>)
}

@ComparisonSort
@StableSort
class BubbleSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        var exchanged: Boolean
        do {
            exchanged = false
            for (i in 1 until arr.size) {
                if (arr[i] < arr[i - 1]) {
                    arr.exch(i, i - 1)
                    exchanged = true
                }
            }
        } while (exchanged)
    }
}

@ComparisonSort
@StableSort
class InsertionSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        for (i in 1 until arr.size) {
            for (j in i downTo 1) {
                if (arr[j - 1] < arr[j]) break
                arr.exch(j, j - 1)
            }
        }
    }
}

@ComparisonSort
@StableSort
class MergeSort<T : Comparable<T>> {

    fun sort(list: MutableList<T>) {
        val newList = mergeSort(list)
        list.clear()
        list.addAll(newList)
    }

    private fun mergeSort(list: List<T>): List<T> {
        if (list.size <= 1) {
            return list
        }

        val middle = list.size / 2
        val left = list.subList(0, middle)
        val right = list.subList(middle, list.size)
        return merge(mergeSort(left), mergeSort(right)).toMutableList()
    }

    private fun merge(left: List<T>, right: List<T>): List<T> {
        var indexLeft = 0
        var indexRight = 0
        val newList: MutableList<T> = mutableListOf()

        while (indexLeft < left.count() && indexRight < right.count()) {
            if (left[indexLeft] <= right[indexRight]) {
                newList.add(left[indexLeft])
                indexLeft++
            } else {
                newList.add(right[indexRight])
                indexRight++
            }
        }

        while (indexLeft < left.size) {
            newList.add(left[indexLeft])
            indexLeft++
        }

        while (indexRight < right.size) {
            newList.add(right[indexRight])
            indexRight++
        }

        return newList
    }
}

@ComparisonSort
@UnstableSort
class QuickSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        sort(arr, 0, arr.size - 1)
    }

    private fun <T : Comparable<T>> sort(arr: MutableList<T>, lo: Int, hi: Int) {
        if (hi <= lo) return
        val j = partition(arr, lo, hi)
        sort(arr, lo, j - 1)
        sort(arr, j + 1, hi)
    }

    private fun <T : Comparable<T>> partition(arr: MutableList<T>, lo: Int, hi: Int): Int {
        var i = lo
        var j = hi + 1
        val v = arr[lo]
        while (true) {
            while (arr[++i] < v) {
                if (i == hi) break
            }
            while (v < arr[--j]) {
                if (j == lo) break
            }
            if (j <= i) break
            arr.exch(j, i)
        }
        arr.exch(j, lo)
        return j
    }
}

@ComparisonSort
@UnstableSort
class SelectionSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        for (i in arr.indices) {
            var min = i
            for (j in i + 1 until arr.size) {
                if (arr[j] < arr[min]) {
                    min = j
                }
            }
            if (min != i) arr.exch(min, i)
        }
    }
}

@ComparisonSort
@StableSort
class ShellSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        var h = 1
        while (h < arr.size / 3) {
            h = h * 3 + 1
        }

        while (h >= 1) {
            for (i in h until arr.size) {
                for (j in i downTo h step h) {
                    if (arr[j - h] < arr[j]) break
                    arr.exch(j, j - h)
                }
            }
            h /= 3
        }
    }
}