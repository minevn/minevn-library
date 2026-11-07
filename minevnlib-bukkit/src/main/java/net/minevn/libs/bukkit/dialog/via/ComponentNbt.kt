package net.minevn.libs.bukkit.dialog.via

import com.viaversion.nbt.tag.Tag
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentCodec
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

/**
 * Đổi Adventure component của server sang NBT component format 1.21.6
 * bằng mcstructs có sẵn trong ViaVersion.
 */
internal object ComponentNbt {
    /**
     * @param component component của server
     * @return NBT của component theo format 1.21.6
     */
    fun toNbt(component: Component): Tag {
        val json = GsonComponentSerializer.gson().serialize(component)
        val parsed = TextComponentCodec.V1_20_5.deserializeJson(json)
        return TextComponentCodec.V1_21_6.serializeNbtTree(parsed)
    }
}
