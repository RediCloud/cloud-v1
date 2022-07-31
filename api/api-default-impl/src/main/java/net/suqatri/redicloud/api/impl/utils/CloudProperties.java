package net.suqatri.redicloud.api.impl.utils;

import lombok.Data;
import lombok.Getter;
import net.suqatri.redicloud.api.utils.ICloudProperties;

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
        return this.properties.getProperty("git-hash", "unknown");
    }

    @Override
    public String getGitBrancheName() {
        return this.properties.getProperty("git-branch", "unknown");
    }

    @Override
    public String getGitCommitMessage() {
        return this.properties.getProperty("git-commit-message", "unknown");
    }
}
