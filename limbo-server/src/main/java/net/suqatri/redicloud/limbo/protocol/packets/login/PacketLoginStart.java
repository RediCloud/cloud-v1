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

package net.suqatri.redicloud.limbo.protocol.packets.login;

import net.suqatri.redicloud.limbo.protocol.ByteMessage;
import net.suqatri.redicloud.limbo.protocol.PacketIn;
import net.suqatri.redicloud.limbo.protocol.registry.Version;
import net.suqatri.redicloud.limbo.connection.ClientConnection;
import net.suqatri.redicloud.limbo.server.LimboServer;

public class PacketLoginStart implements PacketIn {

    private String username;

    public String getUsername() {
        return username;
    }

    @Override
    public void decode(ByteMessage msg, Version version) {
        this.username = msg.readString();
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
