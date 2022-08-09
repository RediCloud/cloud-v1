package net.suqatri.redicloud.node.service.version;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersion;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.StreamUtils;
import net.suqatri.redicloud.commons.file.FileUtils;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import net.suqatri.redicloud.node.console.ConsoleLine;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class NodeCloudServiceVersionManager extends CloudServiceVersionManager {

    public NodeCloudServiceVersionManager(){
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event -> {
            isAnyDefaultVersionInstalled()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to check default version", e))
                .onSuccess(isInstalled -> {
                    if(!isInstalled){
                        CloudAPI.getInstance().getConsole().warn("No default service version is installed!");
                        CloudAPI.getInstance().getConsole().warn("If you want to install a default service version, please run the following command: sv installdefault");
                    }
                });
            });
    }

    @Override
    public ICloudServiceVersion createServiceVersion(ICloudServiceVersion version, boolean fullInstall) throws IOException, InterruptedException {
        ICloudServiceVersion holder = super.createServiceVersion(version, fullInstall);
        if(fullInstall) download(holder, true);
        return holder;
    }

    @Override
    public FutureAction<ICloudServiceVersion> createServiceVersionAsync(ICloudServiceVersion version, boolean fullInstall) {
        FutureAction<ICloudServiceVersion> futureAction = new FutureAction<>();

        createBucketAsync(version.getName(), version)
            .onFailure(futureAction)
            .onSuccess(holder -> {
                if(fullInstall){
                    downloadAsync(holder, true)
                            .onFailure(futureAction)
                            .onSuccess(f -> futureAction.complete(holder));
                }else{
                    futureAction.complete(holder);
                }
            });

        return futureAction;
    }

    @Override
    public boolean download(ICloudServiceVersion holder, boolean force) throws IOException, InterruptedException {
        if (!force && holder.isDownloaded()) return false;
        if(holder.getDownloadUrl().isEmpty()){
            if(!holder.getFile().exists()) throw new FileNotFoundException("File not found: " + holder.getFile().getAbsolutePath());
            return true;
        }
        CloudAPI.getInstance().getConsole().debug("Downloading service version: " + holder.getName());
        File file = holder.getFile();
        String url = holder.getDownloadUrl();
        FileUtils.download(url, file);
        patch(holder, false);
        CloudAPI.getInstance().getConsole().debug("Downloaded service version: " + holder.getName());
        return true;
    }

    @Override
    public FutureAction<Boolean> downloadAsync(ICloudServiceVersion holder, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if (!force && holder.isDownloaded()) {
            futureAction.complete(false);
            return futureAction;
        }

        if(holder.getDownloadUrl().isEmpty()){
            if(!holder.getFile().exists()) {
                futureAction.completeExceptionally(new FileNotFoundException("File not found: " + holder.getFile().getAbsolutePath()));
                return futureAction;
            }
            futureAction.complete(true);
            return futureAction;
        }

        CloudAPI.getInstance().getConsole().debug("Downloading service version: " + holder.getName());

        CloudAPI.getInstance().getExecutorService().submit(() -> {
            File file = holder.getFile();
            String url = holder.getDownloadUrl();
            try {
                FileUtils.download(url, file);
                if (holder.isPaperClip()) {
                    patchAsync(holder, true)
                            .onFailure(futureAction)
                            .onSuccess(b -> {
                                CloudAPI.getInstance().getConsole().debug("Downloaded service version: " + holder.getName());
                                futureAction.complete(true);
                            });
                    return;
                }
                futureAction.complete(true);
            } catch (IOException e) {
                futureAction.completeExceptionally(e);
            }
        });

        return futureAction;
    }

    @Override
    public boolean patch(ICloudServiceVersion holder, boolean force) throws IOException, InterruptedException, IllegalStateException {

        if (!holder.isPaperClip()) return false;

        if (!force && holder.isPatched()) return false;

        logPatch("Patching service version " + holder.getName() + "...");

        UUID id = UUID.randomUUID();
        File processDir = new File(Files.TEMP_VERSION_FOLDER.getFile(), holder.getName() + "-" + id);

        download(holder, false);
        org.apache.commons.io.FileUtils.copyFileToDirectory(holder.getFile(), processDir);

        File jarToPatch = new File(processDir, holder.getName() + ".jar");

        ProcessBuilder builder = new ProcessBuilder(holder.getJavaCommand(), "-jar", jarToPatch.getAbsolutePath());
        builder.directory(processDir);

        Process process = builder.start();
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader reader = new BufferedReader(inputStreamReader);
        while (process.isAlive() && reader.ready()) {
            String line = reader.readLine();
            if (line != null) {
                logPatch(line);
            }
        }
        reader.close();
        int exitCode = process.waitFor();
        CloudAPI.getInstance().getConsole().info("Patch service version " + holder.getName() + " with exit code " + exitCode);

        if (StreamUtils.isOpen(process.getErrorStream())) {
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            boolean error = false;
            while (reader.ready()) {
                String line = reader.readLine();
                error = true;
                if (line != null) {
                    logErrorPatch(line);
                }
            }
            reader.close();
            if (error) {
                logErrorPatch("There was an error while patching service version " + holder.getName() + "!");
                logErrorPatch("Redownloading and repatching jar in few seconds...");
                if (holder.getFile().exists()) holder.getFile().delete();
                return this.patch(holder, true);
            }
        }

        File patchedJar = null;
        File cacheDir = new File(processDir, "cache");
        if (!cacheDir.exists()) {
            patchedJar = jarToPatch;
        }
        for (File file : cacheDir.listFiles()) {
            if (file.getName().startsWith("patch")) {
                patchedJar = file;
                break;
            }
        }
        if (patchedJar == null){
            patchedJar = jarToPatch;
        }

        File patchedJarDest = new File(Files.VERSIONS_FOLDER.getFile(), holder.getName() + ".patched.jar");
        org.apache.commons.io.FileUtils.copyFile(patchedJar, patchedJarDest);

        org.apache.commons.io.FileUtils.deleteDirectory(processDir);

        CloudAPI.getInstance().getConsole().debug("Service version " + holder.getName() + " successfully patched.");
        return true;
    }

    @Override
    public FutureAction<Boolean> patchAsync(ICloudServiceVersion holder, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if (!holder.isPaperClip()) {
            futureAction.completeExceptionally(new IllegalStateException("Service version is not paperclip!"));
            return futureAction;
        }

        if (!force && !holder.needPatch()) {
            futureAction.complete(false);
            return futureAction;
        }

        CloudAPI.getInstance().getConsole().debug("Patching service version " + holder.getName() + "...");

        UUID id = UUID.randomUUID();
        File processDir = new File(Files.TEMP_VERSION_FOLDER.getFile(), holder.getName() + "-" + id);

        downloadAsync(holder, false)
                .onFailure(futureAction)
                .onSuccess(b -> {
                    CloudAPI.getInstance().getExecutorService().submit(() -> {
                        try {
                            org.apache.commons.io.FileUtils.copyFileToDirectory(holder.getFile(), processDir);
                        } catch (IOException e) {
                            futureAction.completeExceptionally(e);
                            return;
                        }
                        File jarToPatch = new File(processDir, holder.getName() + ".jar");

                        ProcessBuilder builder = new ProcessBuilder(holder.getJavaCommand(), "-jar", jarToPatch.getAbsolutePath());
                        builder.directory(processDir);
                        logPatch("Patching service version " + holder.getName() + "...");

                        try {
                            Process process = builder.start();
                            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            while (process.isAlive() && reader.ready()) {
                                String line = reader.readLine();
                                if (line != null) {
                                    logPatch(line);
                                }
                            }
                            reader.close();
                            int exitCode = process.waitFor();
                            CloudAPI.getInstance().getConsole().info("Patch service version " + holder.getName() + " with exit code " + exitCode);

                            if (StreamUtils.isOpen(process.getErrorStream())) {
                                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                boolean error = false;
                                while (reader.ready()) {
                                    String line = reader.readLine();
                                    error = true;
                                    if (line != null) {
                                        logErrorPatch(line);
                                    }
                                }
                                reader.close();
                                if (error) {
                                    logErrorPatch("There was an error while patching service version " + holder.getName() + "!");
                                    logErrorPatch("Redownloading and repatching jar in few seconds...");
                                    if (holder.getFile().exists()) holder.getFile().delete();
                                    this.patchAsync(holder, true)
                                            .onFailure(futureAction)
                                            .onSuccess(futureAction::complete);
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            futureAction.completeExceptionally(e);
                            return;
                        }

                        File patchedJar = null;
                        File cacheDir = new File(processDir, "cache");
                        if (!cacheDir.exists()) {
                            futureAction.completeExceptionally(new NullPointerException("Cache directory not found! Failed to patch service version: " + holder.getName()));
                            return;
                        }
                        for (File file : cacheDir.listFiles()) {
                            if (file.getName().startsWith("patch")) {
                                patchedJar = file;
                                break;
                            }
                        }
                        if (patchedJar == null) {
                            patchedJar = jarToPatch;
                        }

                        File patchedJarDest = new File(Files.VERSIONS_FOLDER.getFile(), holder.getName() + ".patched.jar");
                        try {
                            org.apache.commons.io.FileUtils.copyFile(patchedJar, patchedJarDest);
                            org.apache.commons.io.FileUtils.deleteDirectory(processDir);
                        } catch (Exception e) {
                            futureAction.completeExceptionally(e);
                            return;
                        }


                        CloudAPI.getInstance().getConsole().debug("Service version " + holder.getName() + " successfully patched.");

                        futureAction.complete(true);
                    });
                });

        return futureAction;
    }

    public FutureAction<Boolean> isAnyDefaultVersionInstalled(){
        FutureAction<Boolean> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getExecutorService().submit(() -> {
            for(ICloudServiceVersion versionHolder : this.getServiceVersions()){
                if(versionHolder.isDefaultVersion()){
                    futureAction.complete(true);
                    return;
                }
            }
            futureAction.complete(false);
        });
        return futureAction;
    }

    public FutureAction<Collection<ICloudServiceVersion>> installDefaultVersions(boolean overwrite) {
        FutureAction<Collection<ICloudServiceVersion>> futureAction = new FutureAction<>();

        List<String> names = Arrays.stream(DefaultServiceVersion.values()).parallel().map(DefaultServiceVersion::getName).collect(Collectors.toList());

        FutureActionCollection<String, Boolean> existingVersionsAction = new FutureActionCollection<>();
        for (String name : names) {
            existingVersionsAction.addToProcess(name, this.existsServiceVersionAsync(name));
        }
        existingVersionsAction.process()
                .onFailure(futureAction)
                .onSuccess(existsResults -> {
                    if(overwrite){
                         FutureActionCollection<DefaultServiceVersion, ICloudServiceVersion> editAction = new FutureActionCollection<>();
                         for (String name : names) {
                             DefaultServiceVersion defaultServiceVersion = Arrays.stream(DefaultServiceVersion.values())
                                     .parallel().filter(v -> v.getName().equals(name)).findAny().orElse(null);
                              if(existsResults.get(name)){
                                  FutureAction<ICloudServiceVersion> editFutureAction = new FutureAction<>();

                                  getServiceVersionAsync(name)
                                          .onFailure(editFutureAction)
                                          .onSuccess(versionHolder -> {
                                              CloudServiceVersion version = (CloudServiceVersion) versionHolder;
                                              version.setName(defaultServiceVersion.getName());
                                              version.setDefaultVersion(true);
                                              version.setDownloadUrl(defaultServiceVersion.getUrl());
                                              version.setPaperClip(defaultServiceVersion.isPaperClip());
                                              version.setEnvironmentType(defaultServiceVersion.getEnvironment());
                                              versionHolder.updateAsync()
                                                      .onFailure(editFutureAction)
                                                      .onSuccess(holder -> editFutureAction.complete(versionHolder));
                                          });

                                  editAction.addToProcess(defaultServiceVersion, editFutureAction);
                              }else{
                                  if(existsResults.get(name)) continue;
                                  CloudServiceVersion version = new CloudServiceVersion();
                                  version.setName(defaultServiceVersion.getName());
                                  version.setJavaCommand("java");
                                  version.setDefaultVersion(true);
                                  version.setEnvironmentType(defaultServiceVersion.getEnvironment());
                                  version.setDownloadUrl(defaultServiceVersion.getUrl());
                                  version.setPaperClip(defaultServiceVersion.isPaperClip());
                                  editAction.addToProcess(defaultServiceVersion, this.createServiceVersionAsync(version, false));
                              }
                         }

                         editAction.process()
                             .onFailure(futureAction)
                             .onSuccess(r -> futureAction.complete(r.values()));
                    }else{
                        FutureActionCollection<DefaultServiceVersion, ICloudServiceVersion> versionCreateAction = new FutureActionCollection<>();
                        for (String name : names) {
                            if(existsResults.get(name)) continue;
                            DefaultServiceVersion defaultServiceVersion = Arrays.stream(DefaultServiceVersion.values())
                                    .parallel().filter(v -> v.getName().equals(name)).findAny().orElse(null);
                            CloudServiceVersion version = new CloudServiceVersion();
                            version.setName(defaultServiceVersion.getName());
                            version.setJavaCommand("java");
                            version.setDefaultVersion(true);
                            version.setDownloadUrl(defaultServiceVersion.getUrl());
                            version.setPaperClip(defaultServiceVersion.isPaperClip());
                            version.setEnvironmentType(defaultServiceVersion.getEnvironment());
                            versionCreateAction.addToProcess(defaultServiceVersion, this.createServiceVersionAsync(version, false));
                        }
                        versionCreateAction.process()
                            .onFailure(futureAction)
                            .onSuccess(r -> futureAction.complete(r.values()));
                    }
                });
        return futureAction;
    }

    private void logPatch(String message) {
        CloudAPI.getInstance().getConsole().log(new ConsoleLine("PATCH", message));
    }

    private void logErrorPatch(String message) {
        CloudAPI.getInstance().getConsole().log(new ConsoleLine("ERROR-PATCH", message));
    }
}
