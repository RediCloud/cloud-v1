package net.suqatri.cloud.runner.dependency;

import lombok.Getter;

public enum Repository {

    MVN("https://repo1.maven.org/maven2/"),
    AIKAR("https://repo.aikar.co/content/groups/aikar/"),
    SONATYPE("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    ;

    @Getter
    private final String url;

    Repository(String url){
        this.url = url;
    }

}
