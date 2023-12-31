package net.minevn.libs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MineVNUtilsKtTest {

    @Test
    fun minMaxEpochTimestampTest() {
        System.setProperty("user.timezone", "Asia/Ho_Chi_Minh")
        val (min, max) = minMaxEpochTimestamp("12/2023")
        assertEquals(1701363600, min)
        assertEquals(1704041999, max)
    }
}