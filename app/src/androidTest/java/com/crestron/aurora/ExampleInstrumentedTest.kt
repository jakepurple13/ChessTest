package com.crestron.aurora

import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crestron.aurora.utilities.KUtility
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.crestron.aurora", appContext.packageName)
    }

    @Test
    fun funTesting() {
        Loged.wtf("${KUtility.timeToNextHour()}")
        Loged.wtf("${KUtility.timeToNextHourOrHalf()}")
    }

    @Before
    fun setUp() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
    }
    @Test
    fun socketting() {
        val ssc = ServerSocketChannel.open()
        val s = InetSocketAddress("192.168.94.110", 80)
        ssc.socket().bind(s)
        val sc = SocketChannel.open()
        sc.connect(s)
        ssc.accept().close()
        val buf = arrayOf<ByteBuffer>(ByteBuffer.allocate(10))
        val num = sc.read(buf)
        Loged.wtf("And num is $num")
        assertEquals(-1, num)
        ssc.close()
        sc.close()
    }

    fun addition(num: Int): Int {
        return if(num==1) {
            1
        } else {
            addition(num-1)
        }
    }

    @Test
    fun logedTest() {
        Loged.wtf("${addition(10)}")
        val s = "asdf"
        Loged.v("${s.length} aksdjlhfasjdfalkjsd;lk")
        Loged.v(msg = "asdf", tag = "asdf")

    }

}
