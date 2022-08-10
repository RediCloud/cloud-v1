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

package dev.redicloud.limbo.connection;

import dev.redicloud.limbo.server.LimboServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import dev.redicloud.limbo.connection.pipeline.PacketDecoder;
import dev.redicloud.limbo.connection.pipeline.PacketEncoder;
import dev.redicloud.limbo.connection.pipeline.VarIntFrameDecoder;
import dev.redicloud.limbo.connection.pipeline.VarIntLengthEncoder;

import java.util.concurrent.TimeUnit;

public class ClientChannelInitializer extends ChannelInitializer<Channel> {

    private final LimboServer server;

    public ClientChannelInitializer(LimboServer server) {
        this.server = server;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        PacketDecoder decoder = new PacketDecoder();
        PacketEncoder encoder = new PacketEncoder();
        ClientConnection connection = new ClientConnection(channel, server, decoder, encoder);

        pipeline.addLast("timeout", new ReadTimeoutHandler(server.getConfig().getReadTimeout(),
                TimeUnit.MILLISECONDS));
        pipeline.addLast("frame_decoder", new VarIntFrameDecoder());
        pipeline.addLast("frame_encoder", new VarIntLengthEncoder());
        pipeline.addLast("decoder", decoder);
        pipeline.addLast("encoder", encoder);
        pipeline.addLast("handler", connection);
    }

}
