/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.redicloud.limbo.connection.pipeline;

import dev.redicloud.limbo.protocol.ByteMessage;
import dev.redicloud.limbo.protocol.Packet;
import dev.redicloud.limbo.protocol.PacketSnapshot;
import dev.redicloud.limbo.protocol.registry.State;
import dev.redicloud.limbo.protocol.registry.Version;
import dev.redicloud.limbo.server.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    private State.PacketRegistry registry;
    private Version version;

    public PacketEncoder() {
        updateVersion(Version.getMin());
        updateState(State.HANDSHAKING);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        if (registry == null) return;

        ByteMessage msg = new ByteMessage(out);
        int packetId;

        if (packet instanceof PacketSnapshot) {
            packetId = registry.getPacketId(((PacketSnapshot)packet).getWrappedPacket().getClass());
        } else {
            packetId = registry.getPacketId(packet.getClass());
        }

        if (packetId == -1) {
            Logger.warning("Undefined packet class: %s[0x%s]", packet.getClass().getName(), Integer.toHexString(packetId));
            return;
        }

        msg.writeVarInt(packetId);

        try {
            packet.encode(msg, version);

            if (Logger.getLevel() >= Logger.Level.DEBUG.getIndex()) {
                Logger.debug("Sending %s[0x%s] packet (%d bytes)", packet.toString(), Integer.toHexString(packetId), msg.readableBytes());
            }
        } catch (Exception e) {
            Logger.error("Cannot encode packet 0x%s: %s", Integer.toHexString(packetId), e.getMessage());
        }
    }

    public void updateVersion(Version version) {
        this.version = version;
    }

    public void updateState(State state) {
        this.registry = state.clientBound.getRegistry(version);
    }

}
