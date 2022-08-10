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

package dev.redicloud.limbo.protocol.packets.status;

import dev.redicloud.limbo.connection.ClientConnection;
import dev.redicloud.limbo.protocol.ByteMessage;
import dev.redicloud.limbo.protocol.PacketIn;
import dev.redicloud.limbo.protocol.registry.Version;
import dev.redicloud.limbo.server.LimboServer;

public class PacketStatusRequest implements PacketIn {

    @Override
    public void decode(ByteMessage msg, Version version) {

    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}