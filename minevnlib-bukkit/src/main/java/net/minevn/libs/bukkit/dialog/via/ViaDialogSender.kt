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
    /**
     * @param player player cần kiểm tra
     * @return true nếu Via có kết nối của player và client từ 1.21.6 trở lên
     */
    fun isSupported(player: Player): Boolean {
        val version = Via.getAPI().getPlayerProtocolVersion(player.uniqueId) ?: return false
        return version.newerThanOrEqualTo(ProtocolVersion.v1_21_6) && connection(player) != null
    }

    /**
     * Gửi packet show_dialog, bỏ qua nếu Via không có kết nối của player.
     *
     * @param player player nhận dialog
     * @param dialog NBT inline của dialog
     */
    fun show(player: Player, dialog: CompoundTag) {
        val user = connection(player) ?: return
        val packet = PacketWrapper.create(ClientboundPackets1_21_6.SHOW_DIALOG, user)
        packet.write(Types.TRUSTED_COMPOUND_TAG_HOLDER, Holder.of(dialog))
        packet.send(Protocol1_21_5To1_21_6::class.java)
    }

    /**
     * Gửi packet clear_dialog, bỏ qua nếu Via không có kết nối của player.
     *
     * @param player player cần đóng dialog
     */
    fun clear(player: Player) {
        val user = connection(player) ?: return
        PacketWrapper.create(ClientboundPackets1_21_6.CLEAR_DIALOG, user).send(Protocol1_21_5To1_21_6::class.java)
    }

    /**
     * @param player player cần lấy kết nối
     * @return kết nối của player trong Via, null nếu không có
     */
    private fun connection(player: Player): UserConnection? = Via.getAPI().getConnection(player.uniqueId)
}
