package dev.redicloud.dependency;

import dev.redicloud.dependency.classloader.URLClassPath;
import lombok.Getter;
import net.bytebuddy.agent.ByteBuddyAgent;
import sun.java2d.loops.ProcessPath;
import sun.management.VMManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class DependencyLoader {

    @Getter
    private static DependencyLoader loader;
    private final List<AdvancedDependency> resolvedDependencies;
    private final List<AdvancedDependency> installedDependencies;
    private final List<AdvancedDependency> injectedDependencies;
    private final List<AdvancedDependency> queuedDependencies;
    private final List<String> queuedRepositories;
    private final File dependencyFolder;
    private final File repositoryFolder;
    private final File infoFolder;
    private final File blackListFolder;

    public DependencyLoader(File dependencyFolder, File repositoryFolder, File infoFolder, File blackListFolder) {
        loader = this;
        this.blackListFolder = blackListFolder;
        this.resolvedDependencies = new ArrayList<>();
        this.installedDependencies = new ArrayList<>();
        this.queuedDependencies = new ArrayList<>();
        this.queuedRepositories = Arrays.stream(Repository.values()).parallel().map(Repository::getUrl).collect(Collectors.toList());
        this.injectedDependencies = new ArrayList<>();
        this.dependencyFolder = dependencyFolder;
        this.repositoryFolder = repositoryFolder;
        this.infoFolder = infoFolder;
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public void reset() {
        resolvedDependencies.clear();
    }

    public void loadDependencies() {
        this.loadDependencies(queuedRepositories, queuedDependencies);
        this.queuedDependencies.clear();
        this.queuedRepositories.clear();
        this.queuedRepositories.addAll(Arrays.stream(Repository.values()).parallel().map(Repository::getUrl).collect(Collectors.toList()));
    }

    public void addDependency(AdvancedDependency advancedDependency, String url) {
        this.queuedRepositories.add(url);
        this.queuedDependencies.add(advancedDependency);
    }

    public void addDependency(AdvancedDependency advancedDependency) {
        this.queuedDependencies.add(advancedDependency);
    }

    public List<File> loadDependencies(List<String> repositories, List<AdvancedDependency> dependencies) {
        if (dependencies.isEmpty()) return Collections.emptyList();
        List<String> dependenciesString = dependencies.parallelStream().map(CloudDependency::getName).collect(Collectors.toList());
        for (String s : dependenciesString) {
            System.out.println("- " + s);
        }
        List<AdvancedDependency> allDependencies = new ArrayList<>();
        for (AdvancedDependency dependency : dependencies) {
            allDependencies.addAll(collectSubdependencies(dependency, repositories, new ArrayList<>()));
        }
        List<File> dependencyFiles = allDependencies.parallelStream().map(AdvancedDependency::getDownloadedFile).collect(Collectors.toList());
        for (File dependencyFile : dependencyFiles) {
            System.out.println("- " + dependencyFile.getName());
        }
        installedDependencies.addAll(allDependencies);
        return dependencyFiles;
    }

    private List<AdvancedDependency> collectSubdependencies(AdvancedDependency advancedDependency, List<String> repositories, List<AdvancedDependency> list) {
        File blacklistFile = new File(this.blackListFolder, advancedDependency.getArtifactId() + "-" + advancedDependency.getVersion() + ".blacklist");
        if (blacklistFile.exists()) return list;
        if (this.resolvedDependencies.contains(advancedDependency)) return list;
        this.resolvedDependencies.add(advancedDependency);
        list.add(advancedDependency);
        resolveDependencyFilesIfNotExist(advancedDependency, repositories);
        System.out.println("Loading dependency " + advancedDependency.getName());
        List<AdvancedDependency> subdependencies = getSubDependenciesOfDependency(advancedDependency);
        subdependencies.forEach(sdep -> {
            collectSubdependencies(sdep, repositories, list);
        });
        return list;
    }

    private void resolveDependencyFilesIfNotExist(AdvancedDependency advancedDependency, List<String> repositories) {
        if (!advancedDependency.getDownloadedInfoFile().exists()) {
            new AdvancedDependencyDownloader(repositories).downloadFiles(advancedDependency);
        }
    }

    private List<AdvancedDependency> getSubDependenciesOfDependency(AdvancedDependency advancedDependency) {
        File infoFile = advancedDependency.getDownloadedInfoFile();

        try {
            FileInputStream readData = new FileInputStream(infoFile);
            ObjectInputStream readStream = new ObjectInputStream(readData);

            ArrayList<CloudDependency> dependencies = (ArrayList<CloudDependency>) readStream.readObject();
            readStream.close();

            return dependencies.parallelStream()
                    .filter(cloudDependency -> {
                        File blacklistFile = new File(this.blackListFolder, cloudDependency.getArtifactId() + "-" + cloudDependency.getVersion() + ".blacklist");
                        return !blacklistFile.exists();
                    })
                    .map(cloudDependency ->
                            new AdvancedDependency(cloudDependency.getGroupId(),
                                    cloudDependency.getArtifactId(),
                                    cloudDependency.getVersion()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addJarFiles(List<File> files) throws Exception {
        if(ClassLoader.getSystemClassLoader() instanceof URLClassLoader && getJavaVersion() <= 8) {
            System.out.println("Using java 8 URLClassLoader!");
            List<URL> urls = files.parallelStream().map(file -> {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            try{
                URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                for (URL url : urls) {
                    method.invoke(classLoader, url);
                }
                method.setAccessible(false);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(this.getClass().getClassLoader() instanceof URLClassLoader){
            System.out.println("Using URLClassLoader (" + this.getClass().getClassLoader().getClass().getName() + ") with custom ClassPath!");
            List<URL> urls = files.parallelStream().map(file -> {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            URLClassPath classPath = new URLClassPath((URLClassLoader) this.getClass().getClassLoader());
            for (URL url : urls) {
                classPath.addUrl(url);
            }
        }else {
            System.out.println("Using ByteBuddyAgent!");
            for (File file : files) {
                //TODO
                ByteBuddyAgent.attach(new File("storage/libs/redicloud-dependency-agent.jar"),
                        String.valueOf(getCurrentProcessId()), file.getAbsolutePath());
            }
        }
    }

    private static int getCurrentProcessId() throws Exception {
        return Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }

}
