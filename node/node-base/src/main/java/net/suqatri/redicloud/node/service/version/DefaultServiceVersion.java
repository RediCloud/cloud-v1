package net.suqatri.redicloud.node.service.version;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum DefaultServiceVersion {

    PAPER_1_8_8("paper-1.8.8", "https://api.papermc.io/v2/projects/paper/versions/1.8.8/builds/445/downloads/paper-1.8.8-445.jar", true),
    PAPER_1_9_4("paper-1.9.4", "https://api.papermc.io/v2/projects/paper/versions/1.9.4/builds/775/downloads/paper-1.9.4-775.jar", true),
    PAPER_1_10_2("paper-1.10.2", "https://api.papermc.io/v2/projects/paper/versions/1.10.2/builds/918/downloads/paper-1.10.2-918.jar", true),
    PAPER_1_11_2("paper-1.11.2", "https://api.papermc.io/v2/projects/paper/versions/1.11.2/builds/1106/downloads/paper-1.11.2-1106.jar", true),
    PAPER_1_12_2("paper-1.12.2", "https://api.papermc.io/v2/projects/paper/versions/1.12.2/builds/1620/downloads/paper-1.12.2-1620.jar", true),
    PAPER_1_13_2("paper-1.13.2", "https://api.papermc.io/v2/projects/paper/versions/1.13.2/builds/657/downloads/paper-1.13.2-657.jar", true),
    PAPER_1_14_4("paper-1.14.4", "https://api.papermc.io/v2/projects/paper/versions/1.14.4/builds/245/downloads/paper-1.14.4-245.jar", true),
    PAPER_1_15_2("paper-1.15.2", "https://api.papermc.io/v2/projects/paper/versions/1.15.2/builds/393/downloads/paper-1.15.2-393.jar", true),
    PAPER_1_16_5("paper-1.16.5", "https://api.papermc.io/v2/projects/paper/versions/1.16.5/builds/794/downloads/paper-1.16.5-794.jar", true),
    PAPER_1_17_0("paper-1.17.1", "https://api.papermc.io/v2/projects/paper/versions/1.17.1/builds/411/downloads/paper-1.17.1-411.jar", true),
    PAPER_1_18_2("paper-1.18.2", "https://api.papermc.io/v2/projects/paper/versions/1.18.2/builds/387/downloads/paper-1.18.2-387.jar", true),
    PAPER_1_19_1("paper-1.19.1", "https://api.papermc.io/v2/projects/paper/versions/1.19.1/builds/104/downloads/paper-1.19.1-104.jar", true),

    WATERFALL("waterfall", "https://api.papermc.io/v2/projects/waterfall/versions/1.19/builds/500/downloads/waterfall-1.19-500.jar", false),
    BUNGEECORD("bungeecord", "https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar", false),
    VELOCITY("velocity", "https://api.papermc.io/v2/projects/velocity/versions/3.1.2-SNAPSHOT/builds/165/downloads/velocity-3.1.2-SNAPSHOT-165.jar", false),

    SPIGOT_1_8_8("spigot-1.8.8", "https://cdn.getbukkit.org/spigot/spigot-1.8.8.jar", false),
    SPIGOT_1_9_4("spigot-1.9.4", "https://cdn.getbukkit.org/spigot/spigot-1.9.4.jar", false),
    SPIGOT_1_10_2("spigot-1.10.2", "https://cdn.getbukkit.org/spigot/spigot-1.10.2.jar", false),
    SPIGOT_1_11_2("spigot-1.11.2", "https://cdn.getbukkit.org/spigot/spigot-1.11.2.jar", false),
    SPIGOT_1_12_2("spigot-1.12.2", "https://cdn.getbukkit.org/spigot/spigot-1.12.2.jar", false),
    SPIGOT_1_13_2("spigot-1.13.2", "https://cdn.getbukkit.org/spigot/spigot-1.13.2.jar", false),
    SPIGOT_1_14_4("spigot-1.14.4", "https://cdn.getbukkit.org/spigot/spigot-1.14.4.jar", false),
    SPIGOT_1_15_2("spigot-1.15.2", "https://cdn.getbukkit.org/spigot/spigot-1.15.2.jar", false),
    SPIGOT_1_16_5("spigot-1.16.5", "https://cdn.getbukkit.org/spigot/spigot-1.16.5.jar", false),
    SPIGOT_1_17_1("spigot-1.17.1", "https://cdn.getbukkit.org/spigot/spigot-1.17.1.jar", false),
    SPIGOT_1_18_2("spigot-1.18.2", "https://cdn.getbukkit.org/spigot/spigot-1.18.2.jar", false),
    SPIGOT_1_19_1("spigot-1.19.1", "https://cdn.getbukkit.org/spigot/spigot-1.19.1.jar", false);

    private final String name;
    private final String url;
    private final boolean paperClip;

}
