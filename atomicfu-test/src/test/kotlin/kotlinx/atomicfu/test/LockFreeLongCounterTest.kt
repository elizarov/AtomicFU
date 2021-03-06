package kotlinx.atomicfu.test

import org.junit.Test

class LockFreeLongCounterTest {
    private inline fun testWith(g: LockFreeLongCounter.() -> Long) {
        val c = LockFreeLongCounter()
        check(c.g() == 0L)
        check(c.increment() == 1L)
        check(c.g() == 1L)
        check(c.increment() == 2L)
        check(c.g() == 2L)
    }

    @Test
    fun testBasic() = testWith { get() }

    @Test
    fun testGetInner() = testWith { getInner() }

    @Test
    fun testAdd2() {
        val c = LockFreeLongCounter()
        c.add2()
        check(c.get() == 2L)
        c.add2()
        check(c.get() == 4L)
    }
}