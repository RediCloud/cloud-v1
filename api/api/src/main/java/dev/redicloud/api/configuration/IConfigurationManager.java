package dev.redicloud.api.configuration;

public interface IConfigurationManager {

    <T extends IConfiguration> T getConfiguration(String identifier, Class<T> clazz);

    <T extends IConfiguration> T createConfiguration(T configuration);

    boolean existsConfiguration(String identifier);

    <T extends IConfiguration> boolean deleteConfiguration(T configuration);

    void reloadFromDatabase();

}