package dev.redicloud.node.service.version;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.redicloud.api.service.ServiceEnvironment;

@AllArgsConstructor @Getter
public enum DefaultServiceVersion {

    PAPER_1_8_8("paper-1.8.8", "https://service-versions.redicloud.dev/paper/1.8.8/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_9_4("paper-1.9.4", "https://service-versions.redicloud.dev/paper/1.9.4/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_10_2("paper-1.10.2", "https://service-versions.redicloud.dev/paper/1.10.2/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_11_2("paper-1.11.2", "https://service-versions.redicloud.dev/paper/1.11.2/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_12_2("paper-1.12.2", "https://service-versions.redicloud.dev/paper/1.12.2/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_13_2("paper-1.13.2", "https://service-versions.redicloud.dev/paper/1.13.2/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_14_4("paper-1.14.4", "https://service-versions.redicloud.dev/paper/1.14.4/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_15_2("paper-1.15.2", "https://service-versions.redicloud.dev/paper/1.15.2/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_16_5("paper-1.16.5", "https://service-versions.redicloud.dev/paper/1.16.5/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_17_0("paper-1.17.1", "https://service-versions.redicloud.dev/paper/1.17.1/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_18_2("paper-1.18.2", "https://service-versions.redicloud.dev/paper/1.18.2/latest/download", true, ServiceEnvironment.MINECRAFT),
    PAPER_1_19_2("paper-1.19.2", "https://service-versions.redicloud.dev/paper/1.19.2/latest/download", true, ServiceEnvironment.MINECRAFT),

    WATERFALL("waterfall", "https://service-versions.redicloud.dev/waterfall/1.19/latest/download", false, ServiceEnvironment.BUNGEECORD),
    BUNGEECORD("bungeecord", "https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar", false, ServiceEnvironment.BUNGEECORD),
    VELOCITY("velocity", "https://api.papermc.io/v2/projects/velocity/versions/3.1.2-SNAPSHOT/builds/165/downloads/velocity-3.1.2-SNAPSHOT-165.jar", false, ServiceEnvironment.VELOCITY),

    SPIGOT_1_8_8("spigot-1.8.8", "https://cdn.getbukkit.org/spigot/spigot-1.8.8-R0.1-SNAPSHOT-latest.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_9_4("spigot-1.9.4", "https://cdn.getbukkit.org/spigot/spigot-1.9.4-R0.1-SNAPSHOT-latest.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_10_2("spigot-1.10.2", "https://cdn.getbukkit.org/spigot/spigot-1.10.2-R0.1-SNAPSHOT-latest.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_11_2("spigot-1.11.2", "https://cdn.getbukkit.org/spigot/spigot-1.11.2.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_12_2("spigot-1.12.2", "https://cdn.getbukkit.org/spigot/spigot-1.12.2.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_13_2("spigot-1.13.2", "https://cdn.getbukkit.org/spigot/spigot-1.13.2.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_14_4("spigot-1.14.4", "https://cdn.getbukkit.org/spigot/spigot-1.14.4.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_15_2("spigot-1.15.2", "https://cdn.getbukkit.org/spigot/spigot-1.15.2.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_16_5("spigot-1.16.5", "https://cdn.getbukkit.org/spigot/spigot-1.16.5.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_17_1("spigot-1.17.1", "https://cdn.getbukkit.org/spigot/spigot-1.17.1.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_18_2("spigot-1.18.2", "https://cdn.getbukkit.org/spigot/spigot-1.18.2.jar", false, ServiceEnvironment.MINECRAFT),
    SPIGOT_1_19_1("spigot-1.19.1", "https://cdn.getbukkit.org/spigot/spigot-1.19.1.jar", false, ServiceEnvironment.MINECRAFT),

    LIMBO("limbo", "", false, ServiceEnvironment.LIMBO);

    private final String name;
    private final String url;
    private final boolean paperClip;
    private final ServiceEnvironment environment;

}
