import net.minevn.libs.bukkit.MineVNLib.Companion.parseUIMap
import net.minevn.libs.bukkit.MineVNLib.Companion.toSlotIds
import net.minevn.libs.bukkit.split
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun testParseUIMap() {
        val map = arrayOf(
            "#########",
            "#########",
            "#xxx#xxx#"
        )
        val expected = arrayOf(
            arrayOf(false, false, false, false, false, false, false, false, false),
            arrayOf(false, false, false, false, false, false, false, false, false),
            arrayOf(false, true , true , true , false, true , true , true , false)
        )
        assertArrayEquals(expected, parseUIMap(map))
    }

    @Test
    fun testParsedUISlots() {
        val map = arrayOf(
            "#########",
            "#########",
            "#xxx#xxx#"
        )
        val expected = intArrayOf(19, 20, 21, 23, 24, 25)
        assertArrayEquals(expected, toSlotIds(parseUIMap(map)))
    }

    @Test
    fun testStringSplit() {
        val text = "§f§lA very long text with §c§lcolor §f§lcode to be splitted. §aKeep last color from previous line. " +
                "Convert color symbol to §cdouble S §abefore using this."
        println(text.split(32))
    }
}