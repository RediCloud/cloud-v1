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

package net.suqatri.redicloud.limbo.protocol.packets.play;

import net.suqatri.redicloud.limbo.protocol.ByteMessage;
import net.suqatri.redicloud.limbo.protocol.PacketOut;
import net.suqatri.redicloud.limbo.protocol.registry.Version;

public class PacketTitleSetSubTitle implements PacketOut {

    private String subtitle;

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeString(subtitle);
    }

}