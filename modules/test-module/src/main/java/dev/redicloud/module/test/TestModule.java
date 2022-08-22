package dev.redicloud.module.test;

import dev.redicloud.module.CloudModule;

public class TestModule extends CloudModule {

    @Override
    public void onLoad() {
        getApi().getConsole().info("TestModule loaded !!!");
    }

    @Override
    public void onEnable() {
        getApi().getConsole().info("TestModule enabled !!!");
    }

    @Override
    public void onDisable() {
        getApi().getConsole().info("TestModule disabled !!!");
    }
}
