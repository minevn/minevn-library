package net.minevn.libs

import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun testHttp() {
        println(get("https://kiemtraip.com/raw.php"))
    }
}