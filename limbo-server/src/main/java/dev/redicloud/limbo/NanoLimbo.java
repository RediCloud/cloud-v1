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

package dev.redicloud.limbo;

import dev.redicloud.api.utils.Files;
import dev.redicloud.dependency.DependencyLoader;
import dev.redicloud.limbo.api.LimboCloudAPI;
import dev.redicloud.limbo.server.LimboServer;

public final class NanoLimbo {

    public static void main(String[] args) {
        try {
            LimboServer server = new LimboServer();
            server.start();
            DependencyLoader dependencyLoader = new DependencyLoader(Files.LIBS_FOLDER.getFile(),
                    Files.LIBS_REPO_FOLDER.getFile(),
                    Files.LIBS_INFO_FOLDER.getFile(),
                    Files.LIBS_BLACKLIST_FOLDER.getFile());
            new LimboCloudAPI(dependencyLoader, server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}