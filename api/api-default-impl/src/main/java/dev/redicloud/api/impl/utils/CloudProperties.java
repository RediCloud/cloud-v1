package dev.redicloud.api.impl.utils;

import lombok.Getter;
import dev.redicloud.api.utils.ICloudProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class CloudProperties implements ICloudProperties {

    private final Properties properties;

    public CloudProperties(InputStream inputStream) throws IOException {
        this.properties = new Properties();
        this.properties.load(inputStream);
    }

    @Override
    public String getVersion() {
        return this.properties.getProperty("version", "unknown");
    }

    @Override
    public String getGitHash() {
        return this.properties.getProperty("gitHash", "unknown");
    }

    @Override
    public String getGitBrancheName() {
        return this.properties.getProperty("gitBranch", "unknown");
    }

}
