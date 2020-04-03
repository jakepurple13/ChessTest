@file:Suppress("IncorrectScope")

package com.crestron.aurora

import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.showapi.Source
import com.crestron.aurora.utilities.f
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.apache.tools.ant.util.DateUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Math.floorDiv
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


@Suppress("SameParameterValue")
class TestUnitThree {

    @Before
    fun beforeSetup() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
        Loged.UNIT_TESTING = true
        Loged.OTHER_CLASS_FILTER = { !it.contains("Framing") }
        println("Starting at ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(System.currentTimeMillis())}")
    }

    @After
    fun afterSetup() {
        println("Ending at ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(System.currentTimeMillis())}")
    }

    private suspend fun <A, B> Iterable<A>.pmapped(f: suspend (A) -> B): List<B> = coroutineScope {
        map { async { f(it) } }.awaitAll()
    }

    @UseExperimental(ExperimentalTime::class)
    @Test
    fun mappingTesting() = runBlocking {
        val time = measureTime {
            val output = (1..100).pmapped { it * 2 }
            println(output)
        }.inMilliseconds

        println("Total time: $time")

        val time2 = measureTime {
            val output = (1..100).map { it * 2 }
            println(output)
        }.inMilliseconds

        println("Total time2: $time2")

        val time3 = measureTime {
            val output = (1..100).toList().parallelStream().map { it * 2 }.collect(Collectors.toList())
            println(output)
        }.inMilliseconds

        println("Total time3: $time3")
    }

    private fun getUrl(html: String): String {
        val r = "}\\('(.+)',(\\d+),(\\d+),'([^']+)'\\.split\\('\\|'\\)".toRegex().find(html)!!
        fun encodeBaseN(num: Int, n: Int): String {
            var num1 = num
            val fullTable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val table = fullTable.substring(0..n)
            if (num1 == 0) return table[0].toString()
            var ret = ""
            while (num1 > 0) {
                ret = (table[num1 % n].toString() + ret)
                num1 = floorDiv(num, n)
            }
            return ret
        }
        val (obfucastedCode, baseTemp, countTemp, symbolsTemp) = r.destructured
        val base = baseTemp.toInt()
        var count = countTemp.toInt()
        val symbols = symbolsTemp.split("|")
        val symbolTable = mutableMapOf<String, String>()
        while (count > 0) {
            count--
            val baseNCount = encodeBaseN(count, base)
            symbolTable[baseNCount] = if (symbols[count].isNotEmpty()) symbols[count] else baseNCount
        }
        val unpacked = "\\b(\\w+)\\b".toRegex().replace(obfucastedCode) { symbolTable[it.groups[0]!!.value].toString() }
        val search = "MDCore\\.v.*?=\"([^\"]+)".toRegex().find(unpacked)!!.groups[1]!!.value
        return "https:$search"
    }

    private fun getPutLocker(url: String): String = try {
        val doc = Jsoup.connect(url.trim()).get()
        val mix = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().find(doc.toString())!!.groups[1]!!.value
        val doc2 = Jsoup.connect(mix.trim()).get()
        val r = "}\\('(.+)',(\\d+),(\\d+),'([^']+)'\\.split\\('\\|'\\)".toRegex().find(doc2.toString())!!
        fun encodeBaseN(num: Int, n: Int): String {
            var num1 = num
            val fullTable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val table = fullTable.substring(0..n)
            if (num1 == 0) return table[0].toString()
            var ret = ""
            while (num1 > 0) {
                ret = (table[num1 % n].toString() + ret)
                num1 = floorDiv(num, n)
            }
            return ret
        }
        val (obfucastedCode, baseTemp, countTemp, symbolsTemp) = r.destructured
        val base = baseTemp.toInt()
        var count = countTemp.toInt()
        val symbols = symbolsTemp.split("|")
        val symbolTable = mutableMapOf<String, String>()
        while (count > 0) {
            count--
            val baseNCount = encodeBaseN(count, base)
            symbolTable[baseNCount] = if (symbols[count].isNotEmpty()) symbols[count] else baseNCount
        }
        val unpacked = "\\b(\\w+)\\b".toRegex().replace(obfucastedCode) { symbolTable[it.groups[0]!!.value].toString() }
        val search = "MDCore\\.v.*?=\"([^\"]+)".toRegex().find(unpacked)!!.groups[1]!!.value
        "https:$search"
    } catch (e: Exception) {
        ""
    }

    @Test
    fun putlo() {
        val f = ShowApi(Source.LIVE_ACTION).showInfoList
        val e = f.find { it.name.contains("HarmonQuest", true) }?.let { EpisodeApi(it) }!!
        Loged.f(e)
        val first = e.episodeList.first()
        Loged.f(first)
        Loged.f(first.getVideoLink())
    }

    @Test
    fun randomVideoInfo() {
        val f = EpisodeApi(ShowApi.getAll().random())
        Loged.f(f)
        /*val a = ShowApi.getSources(Source.LIVE_ACTION_MOVIES).sortedBy { it.name }
        Loged.f(a.count())
        val a1 = a.indexOfFirst { it.name[0].toString().equals("i", true) }
        Loged.f(a1)*/
    }

    @Throws(IOException::class)
    private fun getHtml(url: String): String {
        // Build and set timeout values for the request.
        val connection = URL(url).openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
        connection.addRequestProperty("Accept-Language", "en-US,en;q=0.5")
        connection.addRequestProperty("Referer", "http://thewebsite.com")
        connection.connect()
        // Read and store the result line by line then return the entire string.
        val in1 = connection.getInputStream()
        val reader = BufferedReader(InputStreamReader(in1))
        val html = StringBuilder()
        var line: String? = ""
        while (line != null) {
            line = reader.readLine()
            html.append(line)
        }
        in1.close()

        return html.toString()
    }

    @Test
    fun merryChristmas() {
        /*println(getTree(3))
        println()
        println(getTree(4))
        println()
        println(getTree(5))*/
        //Loged.f(1.christmasTree())
        Loged.f(Random.nextInt(1, 10).christmasTree(), "Tree")
        //(1..10).forEach { Loged.f(it.christmasTree(), "Tree") }
    }

    private fun Number.christmasTree() = getTree(toInt())

    private fun getTree(n: Int): String =
            "${(1..(n * 2) step 2).joinToString("\n") { n1 -> " ".repeat(((n * 2) - n1) / 2).let { "$it${"*".repeat(n1)}$it" } }}\n${" ".repeat(n - 1)}*"

    /*private fun getTree(n: Int): String = when {
        n < 1 -> throw IllegalArgumentException("Must be greater than 0")
        else -> "${(1..(n * 2) step 2).joinToString("\n") { n1 -> " ".repeat(((n * 2) - n1) / 2).let { "$it${"*".repeat(n1)}$it" } }}\n${" ".repeat(n - 1)}*"
    }*/

    @Test
    fun packedTest() {
        val s = """
            eval(function(p,a,c,k,e,r){e=String;if(!''.replace(/^/,String)){while(c--)r[c]=k[c]||c;k=[function(e){return r[e]}];e=function(){return'\\w+'};c=1};while(c--)if(k[c])p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c]);return p}('3 4(0,1,2){5 0.6(7 8(1,\'9\'),2)}',10,10,'a|b|c|function|replaceAll|return|replace|new|RegExp|g'.split('|'),0,{}))
        """.trimIndent()
        val j = JSUnpacker()
        println(j.unpack(s))
    }

    @Test
    fun putMov() {
        val f = ShowApi(Source.LIVE_ACTION_MOVIES).showInfoList
        val e = EpisodeApi(f.random())
        prettyLog(e.episodeList)
        prettyLog(e.episodeList.firstOrNull()?.getVideoInfo())
    }

    @Test
    fun putMov2() {
        val f = ShowApi(Source.LIVE_ACTION_MOVIES).getShowList { println(it) }
        prettyLog(f.size)
    }

    @Test
    fun showAptest() = runBlocking {
        //TODO: Look into multithreading
        val doc: Document = Jsoup.connect("https://www1.putlocker.fyi/a-z-movies").get()
        val alphabet = doc.allElements.select("ul.pagination-az").select("a.page-link")
        val f = alphabet.pmap { p ->
            println(p.attr("abs:href"))
            val page = Jsoup.connect(p.attr("abs:href")).get()
            val listPage = page.allElements.select("li.page-item")
            val lastPage = listPage[listPage.size - 2].text().toInt()
            fun getMovieFromPage(document: Document) = document.allElements.select("div.col-6").map {
                ShowInfo(it.select("span.mov_title").text(), it.select("a.thumbnail").attr("abs:href"))
            }
            (1..lastPage).pmap {
                if (it == 1)
                    getMovieFromPage(page)
                else
                    getMovieFromPage(Jsoup.connect("https://www1.putlocker.fyi/a-z-movies/page/$it/${p.attr("abs:href").split("/").last()}").get())
            }.flatten()
        }.flatten()
        f.showInfo()
    }

    @Test
    fun showApTest2() = runBlocking {
        val doc: Document = Jsoup.connect("https://www1.putlocker.fyi/a-z-movies").get()
        val list = arrayListOf<ShowInfo>()
        val alphabet = doc.allElements.select("ul.pagination-az").select("a.page-link")
        for (p in alphabet) {
            println(p.attr("abs:href") + " and list size is ${list.size}")
            val page = Jsoup.connect(p.attr("abs:href")).get()
            val listPage = page.allElements.select("li.page-item")
            val lastPage = listPage[listPage.size - 2].text().toInt()
            fun getMovieFromPage(document: Document) = list.addAll(document.allElements.select("div.col-6").map {
                ShowInfo(
                        it.select("span.mov_title").text(),
                        it.select("a.thumbnail").attr("abs:href"))
            })
            getMovieFromPage(page)
            for (i in 2..lastPage) {
                getMovieFromPage(
                        Jsoup.connect("https://www1.putlocker.fyi/a-z-movies/page/$i/${p.attr("abs:href").split("/").last()}").get()
                )
            }
        }
        list.showInfo()
    }

    private fun List<ShowInfo>.showInfo() {
        println("$size")
    }

    fun <T, R> Iterable<T>.pmap(
            numThreads: Int = Runtime.getRuntime().availableProcessors() - 2,
            exec: ExecutorService = Executors.newFixedThreadPool(numThreads),
            transform: (T) -> R): List<R> {
        // default size is just an inlined version of kotlin.collections.collectionSizeOrDefault
        val defaultSize = if (this is Collection<*>) this.size else 10
        val destination = Collections.synchronizedList(ArrayList<R>(defaultSize))

        for (item in this) {
            exec.submit { destination.add(transform(item)) }
        }

        exec.shutdown()
        exec.awaitTermination(1, TimeUnit.DAYS)

        return ArrayList<R>(destination)
    }


    open class Person(open var name: String? = null,
                      open var age: Int? = null,
                      open var dob: Date? = null,
                      open var address: Address? = null) {

        val friends: MutableList<Person> = mutableListOf()

        private fun getDateString() = dob?.let { ", ${SimpleDateFormat("MM-dd-yyyy").format(it)}" } ?: ""

        override fun toString(): String {
            return "$name, $age${getDateString()}. Lives at $address. His friends are ${friends.joinToString(", ") { "${it.name}" }}."
        }
    }

    data class Address(var street: String? = null,
                       var number: Int? = null,
                       var city: String? = null,
                       var hobby: Hobby? = null) {
        override fun toString(): String {
            return "$number $street, $city. His hobby is $hobby"
        }
    }

    data class Hobby(var hobbyName: String? = null) {
        override fun toString(): String {
            return "$hobbyName"
        }
    }

    fun person(block: Person.() -> Unit): Person = Person().apply(block)

    fun Person.address(block: Address.() -> Unit) {
        address = Address().apply(block)
    }

    fun Person.friend(block: Person.() -> Unit) {
        friends.add(Person().apply(block))
    }

    fun Address.hobby(block: Hobby.() -> Unit) {
        hobby = Hobby().apply(block)
    }

    fun personBuilder(block: PersonBuilder.() -> Unit): Person = PersonBuilder().apply(block).build()

    class PersonBuilder {
        var name: String = ""
        var age: Int = 0
        private var dob: Date = Date()
        var dateOfBirth: String = ""
            set(value) {
                dob = SimpleDateFormat("MM-dd-yyyy").parse(value)!!
            }
        private var address: Address? = null
        private var friendList = mutableListOf<Person>()

        fun address(block: AddressBuilder.() -> Unit) {
            address = AddressBuilder().apply(block).build()
        }

        fun friend(block: PersonBuilder.() -> Unit) {
            friendList.add(PersonBuilder().apply(block).build())
        }

        fun build(): Person = Person(name, age, dob, address).apply {
            friends.addAll(friendList)
        }
    }

    class AddressBuilder {
        var street: String = ""
        var number: Int = 0
        var city: String = ""
        private var hobby: Hobby? = null
        fun hobby(block: Hobby.() -> Unit) {
            hobby = Hobby().apply(block)
        }

        fun build(): Address = Address(street, number, city, hobby)
    }

    @Test
    fun dslTesting() {

        val person2 = personBuilder {
            name = "John"
            age = 34
            dateOfBirth = "12-4-2014"
            address {
                street = "Main Street"
                number = 42
                city = "London"
                hobby {
                    hobbyName = "Tennis"
                }
            }
            friend {
                name = "Jacob"
                age = 22
                dateOfBirth = "12-31-1995"
                address {
                    street = "Bedford Rd"
                    number = 861
                    city = "Pleasantville"
                    hobby {
                        hobbyName = "Programming"
                    }
                }
            }
            friend {
                name = "Jordan"
                age = 24
                dateOfBirth = "12-4-1974"
                address {
                    street = "Main Rd"
                    number = 861
                    city = "DC"
                }
            }
        }

        prettyLog(person2)

        val person = person {
            name = "John"
            age = 25
            dob = SimpleDateFormat("MM-dd-yyyy").parse("1-1-1985")!!
            address {
                street = "Main Street"
                number = 42
                city = "London"
                hobby {
                    hobbyName = "Tennis"
                }
            }
            friend {
                name = "Jacob"
                age = 22
                address {
                    street = "Bedford Rd"
                    number = 861
                    city = "Pleasantville"
                    hobby {
                        hobbyName = "Programming"
                    }
                }
                friends.add(this@person)
            }
        }
        prettyLog(person)
        prettyLog(person.friends[0])
    }

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
        } catch (e: NullPointerException) {
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

class JSUnpacker {
    fun unpack(html: String): String {
        val r = "}\\('(.+)',(\\d+),(\\d+),'([^']+)'\\.split\\('\\|'\\)".toRegex().find(html)!!
        fun encodeBaseN(num: Int, n: Int): String {
            var num1 = num
            val fullTable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val table = fullTable.substring(0..n)
            if (num1 == 0) return table[0].toString()
            var ret = ""
            while (num1 > 0) {
                ret = (table[num1 % n].toString() + ret)
                num1 = floorDiv(num, n)
            }
            return ret
        }
        val (obfucastedCode, baseTemp, countTemp, symbolsTemp) = r.destructured
        val base = baseTemp.toInt()
        var count = countTemp.toInt()
        val symbols = symbolsTemp.split("|")
        val symbolTable = mutableMapOf<String, String>()
        while (count > 0) {
            count--
            val baseNCount = encodeBaseN(count, base)
            symbolTable[baseNCount] = if (symbols[count].isNotEmpty()) symbols[count] else baseNCount
        }
        return "\\b(\\w+)\\b".toRegex().replace(obfucastedCode) { symbolTable[it.groups[0]!!.value].toString() }
    }
}
