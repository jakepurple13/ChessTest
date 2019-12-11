package com.crestron.aurora

import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.showapi.Source
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import kotlinx.coroutines.runBlocking
import org.apache.tools.ant.util.DateUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@Suppress("SameParameterValue")
class TestUnitThree {

    @Before
    fun beforeSetup() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
        Loged.UNIT_TESTING = true
        println("Starting at ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(System.currentTimeMillis())}")
    }

    @After
    fun afterSetup() {
        println("Ending at ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(System.currentTimeMillis())}")
    }

    sealed class AndroidInfo(val title: String, val author: String, val description: String, val url: String) {
        open fun isAnyEmpty() = title.isNotBlank() && description.isNotBlank() && author.isNotBlank()
        private val type by lazy { this::javaClass.get().simpleName }
        override fun toString() = "$type - $url | $title by $author: $description"

        class AndroidArsenal(title: String, author: String, description: String, url: String, val dateRegistered: String) : AndroidInfo(title, author, description, url) {
            override fun isAnyEmpty() = super.isAnyEmpty() && dateRegistered.isNotBlank()
            override fun toString() = "${super.toString()} - created on $dateRegistered"
        }

        class GitHubTopic(title: String, author: String, description: String, url: String) : AndroidInfo(title, author, description, url)

        companion object {
            private fun tryCatch(block: () -> String) = try {
                block()
            } catch (e: Exception) {
                ""
            }

            fun getAll(githubTopic: String = "android-library") = getAndroidArsenal() + getGithub(githubTopic)

            fun getAndroidArsenal(): List<AndroidArsenal> = Jsoup
                    .connect("https://android-arsenal.com/?sort=updated&category=1").get()
                    .select("div.pi").map {
                        val url = it.select("div.title a").attr("abs:href")
                        val title = tryCatch { it.select("div.title").select("a").first().text() }
                        val desc = it.select("div.desc").text()
                        val ftr = it.select("div.ftr")
                        val dateRegistered = tryCatch { ftr.first().text() }
                        val author = tryCatch { ftr.last().text() }
                        AndroidArsenal(title, author, desc, url, dateRegistered)
                    }.filter(AndroidArsenal::isAnyEmpty)

            fun getGithub(topic: String = "android-library"): List<GitHubTopic> = Jsoup
                    .connect("https://github.com/topics/${topic.replace(" ", "-")}?o=desc&s=updated").get()
                    .select("article").map {
                        val url = tryCatch { it.select("h1.f3 a").last().attr("abs:href") }
                        val title = it.select("h1.f3").text().split("/")
                        val author = tryCatch { title[0].trim() }
                        val repoTitle = tryCatch { title[1].trim() }
                        val desc = it.select("div.border-bottom div.px-3 p.mb-0").text().trim()
                        GitHubTopic(repoTitle, author, desc, url)
                    }.filter(GitHubTopic::isAnyEmpty)
        }
    }

    @Test
    fun bothTest() {
        val f = AndroidInfo.getAll()
        Loged.f(f)
    }

    @Test
    fun putlo() {
        val f = ShowApi(Source.LIVE_ACTION).showInfoList
        val e = EpisodeApi(f.random())
        prettyLog(e.episodeList)
        prettyLog(e.episodeList.firstOrNull()?.getVideoInfo())
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