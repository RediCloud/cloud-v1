package net.suqatri.cloud.runner.dependency;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class CloudDependency implements Serializable {

    private final String groupId;
    private final String artifactId;
    private final String version;

    public CloudDependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getName() {
        return this.artifactId + "-" + this.version;
    }

    @Override
    public boolean equals(Object dependency){
        if(dependency == null) return false;
        if(!(dependency instanceof CloudDependency)) return false;
        return this.groupId.equals(((CloudDependency) dependency).getGroupId())
                && this.artifactId.equals(((CloudDependency) dependency).getArtifactId())
                && this.version.equals(((CloudDependency) dependency).getVersion());
    }

    @Override
    public String toString() {
        return "CloudDependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
