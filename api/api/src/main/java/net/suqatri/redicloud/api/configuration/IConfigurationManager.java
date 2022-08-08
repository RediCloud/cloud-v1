package net.suqatri.redicloud.api.configuration;

public interface IConfigurationManager {

    <T extends IConfiguration> T getConfiguration(String identifier, Class<T> clazz);

    <T extends IConfiguration> T create(T configuration);

    boolean exists(String identifier);

    <T extends IConfiguration> boolean delete(T configuration);
    boolean delete(String identifier);

    void reloadFromDatabase();

}