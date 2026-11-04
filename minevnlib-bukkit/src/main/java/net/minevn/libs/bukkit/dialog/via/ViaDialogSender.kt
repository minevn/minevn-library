package net.minevn.libs.bukkit.dialog.via

import com.viaversion.nbt.tag.CompoundTag
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.connection.UserConnection
import com.viaversion.viaversion.api.minecraft.Holder
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import com.viaversion.viaversion.api.type.Types
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.Protocol1_21_5To1_21_6
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6
import org.bukkit.entity.Player

/**
 * Gửi packet dialog 1.21.6 thẳng qua pipeline của ViaVersion.
 * Server cũ hơn 1.21.6 không có packet này, nên dựng packet ở tầng protocol 1.21.5 -> 1.21.6
 * và send(Protocol1_21_5To1_21_6), các protocol sau đó (client mới hơn 1.21.6) vẫn được Via dịch tiếp.
 */
internal object ViaDialogSender {
    fun isSupported(player: Player): Boolean {
        val version = Via.getAPI().getPlayerProtocolVersion(player.uniqueId) ?: return false
        return version.newerThanOrEqualTo(ProtocolVersion.v1_21_6) && connection(player) != null
    }

    fun show(player: Player, dialog: CompoundTag) {
        val user = connection(player) ?: return
        val packet = PacketWrapper.create(ClientboundPackets1_21_6.SHOW_DIALOG, user)
        packet.write(Types.TRUSTED_COMPOUND_TAG_HOLDER, Holder.of(dialog))
        packet.send(Protocol1_21_5To1_21_6::class.java)
    }

    fun clear(player: Player) {
        val user = connection(player) ?: return
        PacketWrapper.create(ClientboundPackets1_21_6.CLEAR_DIALOG, user).send(Protocol1_21_5To1_21_6::class.java)
    }

    private fun connection(player: Player): UserConnection? = Via.getAPI().getConnection(player.uniqueId)
}
