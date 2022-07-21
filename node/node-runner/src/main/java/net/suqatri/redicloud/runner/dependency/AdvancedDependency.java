package net.suqatri.redicloud.runner.dependency;

import net.suqatri.redicloud.runner.dependency.utils.Downloader;
import net.suqatri.redicloud.runner.dependency.utils.WebContentLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedDependency extends CloudDependency {

    public AdvancedDependency(String groupId, String artifactId, String version) {
        super(groupId, artifactId, version);
    }

    public URL toURL() throws MalformedURLException {
        return getDownloadedFile().toURI().toURL();
    }

    private String getDownloadUrl(String repoUrl){
        return getUrlWithoutExtension(repoUrl) + ".jar";
    }

    public File getDownloadedFile(){
        return new File(DependencyLoader.getLoader().getDependencyFolder(), getName() + ".jar");
    }

    public File getDownloadedInfoFile(){
        return new File(DependencyLoader.getLoader().getInfoFolder(), getName() + ".info");
    }

    public boolean existInRepo(String repoUrl){
        return WebContentLoader.loadContent(getDownloadUrl(repoUrl)) != null;
    }

    public void download(String repoUrl) throws IOException {
        Downloader.userAgentDownload(this.getDownloadUrl(repoUrl), getDownloadedFile());
    }

    public void download(String repoUrl, File downloadFile) throws IOException {
        Downloader.userAgentDownload(this.getDownloadUrl(repoUrl), downloadFile);
    }

    private String getMainUrl(String repoUrl){
        return repoUrl + getGroupId().replaceAll("\\.", "/") + "/" + getArtifactId() + "/";
    }

    private String getUrlWithoutExtension(String repoUrl){
        return getMainUrl(repoUrl) + getVersion() + "/" + getArtifactId() + "-" + getVersion();
    }

    public AdvancedDependency getDependencyWithNewerVersion(AdvancedDependency other){
        Integer[] dependencyVersion = (Integer[]) getVersionStringAsIntArray(getVersion()).toArray();
        Integer[] otherDependencyVersion = (Integer[]) getVersionStringAsIntArray(other.getVersion()).toArray();
        if (dependencyVersion[0] > otherDependencyVersion[0]) return this;
        if (otherDependencyVersion[0] > dependencyVersion[0]) return other;

        if (dependencyVersion[1] > otherDependencyVersion[1]) return this;
        if (otherDependencyVersion[1] > dependencyVersion[1]) return other;

        if (dependencyVersion[2] > otherDependencyVersion[2]) return this;
        if (otherDependencyVersion[2] > dependencyVersion[2]) return other;

        return this;
    }

    private List<Integer> getVersionStringAsIntArray(String version){
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        String versionParts1 = versionParts.length == 2 ? versionParts[1] : null;
        int minor = parseVersionPart(versionParts1);
        String versionParts2 = versionParts.length == 3 ? versionParts[2] : null;
        int patch  = parseVersionPart(versionParts2);
        return Arrays.asList(major, minor, patch);
    }

    private Integer parseVersionPart(String part) {
        if (part == null) return 0;
        char[] charArray = part.toCharArray();
        List<Integer> numbers = new ArrayList<>();
        for (char c : charArray) {
            if (Character.isDigit(c)) {
                numbers.add(Character.getNumericValue(c));
            } else {
                break;
            }
        }
        if (numbers.isEmpty()) return 0;
        StringBuilder builder = new StringBuilder();
        for (Integer number : numbers) {
            builder.append(number);
        }
        return Integer.parseInt(builder.toString());
    }

    public static AdvancedDependency fromCoords(String coords){
        String[] split = coords.split(":");
        if(split.length != 3) return null;
        return new AdvancedDependency(split[0], split[1], split[2]);
    }
}
