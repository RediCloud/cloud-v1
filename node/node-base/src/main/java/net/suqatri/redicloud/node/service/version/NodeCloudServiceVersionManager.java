package net.suqatri.redicloud.node.service.version;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersion;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.StreamUtils;
import net.suqatri.redicloud.commons.file.FileUtils;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import net.suqatri.redicloud.node.console.ConsoleLine;
import org.checkerframework.checker.units.qual.C;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public IRBucketHolder<ICloudServiceVersion> createServiceVersion(ICloudServiceVersion version, boolean fullInstall) throws IOException, InterruptedException {
        IRBucketHolder<ICloudServiceVersion> holder = super.createServiceVersion(version, fullInstall);
        if(fullInstall) download(holder, true);
        return holder;
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudServiceVersion>> createServiceVersionAsync(ICloudServiceVersion version, boolean fullInstall) {
        FutureAction<IRBucketHolder<ICloudServiceVersion>> futureAction = new FutureAction<>();

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
    public boolean download(IRBucketHolder<ICloudServiceVersion> holder, boolean force) throws IOException, InterruptedException {
        if (!force && holder.get().isDownloaded()) return false;
        CloudAPI.getInstance().getConsole().debug("Downloading service version: " + holder.get().getName());
        File file = holder.get().getFile();
        String url = holder.get().getDownloadUrl();
        FileUtils.download(url, file);
        patch(holder, false);
        CloudAPI.getInstance().getConsole().debug("Downloaded service version: " + holder.get().getName());
        return true;
    }

    @Override
    public FutureAction<Boolean> downloadAsync(IRBucketHolder<ICloudServiceVersion> holder, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if (!force && holder.get().isDownloaded()) {
            futureAction.complete(false);
            return futureAction;
        }

        CloudAPI.getInstance().getConsole().debug("Downloading service version: " + holder.get().getName());

        CloudAPI.getInstance().getExecutorService().submit(() -> {
            File file = holder.get().getFile();
            String url = holder.get().getDownloadUrl();
            try {
                FileUtils.download(url, file);
                if (holder.get().isPaperClip()) {
                    patchAsync(holder, true)
                            .onFailure(futureAction)
                            .onSuccess(b -> {
                                CloudAPI.getInstance().getConsole().debug("Downloaded service version: " + holder.get().getName());
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
    public boolean patch(IRBucketHolder<ICloudServiceVersion> holder, boolean force) throws IOException, InterruptedException, IllegalStateException {

        if (!holder.get().isPaperClip()) return false;

        if (!force && holder.get().isPatched()) return false;

        logPatch("Patching service version " + holder.get().getName() + "...");

        UUID id = UUID.randomUUID();
        File processDir = new File(Files.TEMP_VERSION_FOLDER.getFile(), holder.get().getName() + "-" + id);

        download(holder, false);
        org.apache.commons.io.FileUtils.copyFileToDirectory(holder.get().getFile(), processDir);

        File jarToPatch = new File(processDir, holder.get().getName() + ".jar");

        ProcessBuilder builder = new ProcessBuilder(holder.get().getJavaCommand(), "-jar", jarToPatch.getAbsolutePath());
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
        CloudAPI.getInstance().getConsole().info("Patch service version " + holder.get().getName() + " with exit code " + exitCode);

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
                logErrorPatch("There was an error while patching service version " + holder.get().getName() + "!");
                logErrorPatch("Redownloading and repatching jar in few seconds...");
                if (holder.get().getFile().exists()) holder.get().getFile().delete();
                return this.patch(holder, true);
            }
        }

        File patchedJar = null;
        File cacheDir = new File(processDir, "cache");
        if (!cacheDir.exists())
            throw new NullPointerException("Cache directory not found! Failed to patch service version: " + holder.get().getName());
        for (File file : cacheDir.listFiles()) {
            if (file.getName().startsWith("patch")) {
                patchedJar = file;
                break;
            }
        }
        if (patchedJar == null)
            throw new NullPointerException("Patched jar not found! Failed to patch service version: " + holder.get().getName());

        File patchedJarDest = new File(Files.VERSIONS_FOLDER.getFile(), holder.get().getName() + ".patched.jar");
        org.apache.commons.io.FileUtils.copyFile(patchedJar, patchedJarDest);

        org.apache.commons.io.FileUtils.deleteDirectory(processDir);

        CloudAPI.getInstance().getConsole().debug("Service version " + holder.get().getName() + " successfully patched.");
        return true;
    }

    @Override
    public FutureAction<Boolean> patchAsync(IRBucketHolder<ICloudServiceVersion> holder, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if (!holder.get().isPaperClip()) {
            futureAction.completeExceptionally(new IllegalStateException("Service version is not paperclip!"));
            return futureAction;
        }

        if (!force && !holder.get().needPatch()) {
            futureAction.complete(false);
            return futureAction;
        }

        CloudAPI.getInstance().getConsole().debug("Patching service version " + holder.get().getName() + "...");

        UUID id = UUID.randomUUID();
        File processDir = new File(Files.TEMP_VERSION_FOLDER.getFile(), holder.get().getName() + "-" + id);

        downloadAsync(holder, false)
                .onFailure(futureAction)
                .onSuccess(b -> {
                    CloudAPI.getInstance().getExecutorService().submit(() -> {
                        try {
                            org.apache.commons.io.FileUtils.copyFileToDirectory(holder.get().getFile(), processDir);
                        } catch (IOException e) {
                            futureAction.completeExceptionally(e);
                            return;
                        }
                        File jarToPatch = new File(processDir, holder.get().getName() + ".jar");

                        ProcessBuilder builder = new ProcessBuilder(holder.get().getJavaCommand(), "-jar", jarToPatch.getAbsolutePath());
                        builder.directory(processDir);
                        logPatch("Patching service version " + holder.get().getName() + "...");

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
                            CloudAPI.getInstance().getConsole().info("Patch service version " + holder.get().getName() + " with exit code " + exitCode);

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
                                    logErrorPatch("There was an error while patching service version " + holder.get().getName() + "!");
                                    logErrorPatch("Redownloading and repatching jar in few seconds...");
                                    if (holder.get().getFile().exists()) holder.get().getFile().delete();
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
                            futureAction.completeExceptionally(new NullPointerException("Cache directory not found! Failed to patch service version: " + holder.get().getName()));
                            return;
                        }
                        for (File file : cacheDir.listFiles()) {
                            if (file.getName().startsWith("patch")) {
                                patchedJar = file;
                                break;
                            }
                        }
                        if (patchedJar == null) {
                            futureAction.completeExceptionally(new NullPointerException("Patched jar not found! Failed to patch service version: " + holder.get().getName()));
                            return;
                        }

                        File patchedJarDest = new File(Files.VERSIONS_FOLDER.getFile(), holder.get().getName() + ".patched.jar");
                        try {
                            org.apache.commons.io.FileUtils.copyFile(patchedJar, patchedJarDest);
                            org.apache.commons.io.FileUtils.deleteDirectory(processDir);
                        } catch (Exception e) {
                            futureAction.completeExceptionally(e);
                            return;
                        }


                        CloudAPI.getInstance().getConsole().debug("Service version " + holder.get().getName() + " successfully patched.");

                        futureAction.complete(true);
                    });
                });

        return futureAction;
    }

    public FutureAction<Boolean> isAnyDefaultVersionInstalled(){
        FutureAction<Boolean> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getExecutorService().submit(() -> {
            for(IRBucketHolder<ICloudServiceVersion> versionHolder : this.getServiceVersions()){
                if(versionHolder.get().isDefaultVersion()){
                    futureAction.complete(true);
                    return;
                }
            }
            futureAction.complete(false);
        });
        return futureAction;
    }

    public FutureAction<Collection<IRBucketHolder<ICloudServiceVersion>>> installDefaultVersions(boolean overwrite) {
        FutureAction<Collection<IRBucketHolder<ICloudServiceVersion>>> futureAction = new FutureAction<>();

        List<String> names = Arrays.stream(DefaultServiceVersion.values()).parallel().map(DefaultServiceVersion::getName).collect(Collectors.toList());

        FutureActionCollection<String, Boolean> existingVersionsAction = new FutureActionCollection<>();
        for (String name : names) {
            existingVersionsAction.addToProcess(name, this.existsServiceVersionAsync(name));
        }
        existingVersionsAction.process()
                .onFailure(futureAction)
                .onSuccess(existsResults -> {
                    if(overwrite){
                         FutureActionCollection<DefaultServiceVersion, IRBucketHolder<ICloudServiceVersion>> editAction = new FutureActionCollection<>();
                         for (String name : names) {
                             DefaultServiceVersion defaultServiceVersion = Arrays.stream(DefaultServiceVersion.values())
                                     .parallel().filter(v -> v.getName().equals(name)).findAny().orElse(null);
                              if(existsResults.get(name)){
                                  FutureAction<IRBucketHolder<ICloudServiceVersion>> editFutureAction = new FutureAction<>();

                                  getServiceVersionAsync(name)
                                          .onFailure(editFutureAction)
                                          .onSuccess(versionHolder -> {
                                              versionHolder.getImpl(CloudServiceVersion.class).setName(defaultServiceVersion.getName());
                                              versionHolder.getImpl(CloudServiceVersion.class).setDefaultVersion(true);
                                              versionHolder.getImpl(CloudServiceVersion.class).setDownloadUrl(defaultServiceVersion.getUrl());
                                              versionHolder.getImpl(CloudServiceVersion.class).setPaperClip(defaultServiceVersion.isPaperClip());
                                              versionHolder.getImpl(CloudServiceVersion.class).setEnvironmentType(defaultServiceVersion.getEnvironment());
                                              versionHolder.get().updateAsync()
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
                                  version.setDownloadUrl(defaultServiceVersion.getUrl());
                                  version.setPaperClip(defaultServiceVersion.isPaperClip());
                                  editAction.addToProcess(defaultServiceVersion, this.createServiceVersionAsync(version, false));
                              }
                         }

                         editAction.process()
                             .onFailure(futureAction)
                             .onSuccess(r -> futureAction.complete(r.values()));
                    }else{
                        FutureActionCollection<DefaultServiceVersion, IRBucketHolder<ICloudServiceVersion>> versionCreateAction = new FutureActionCollection<>();
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
