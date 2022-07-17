package net.suqatri.cloud.node.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceCopier;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.version.ICloudServiceVersion;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.file.FileEditor;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;
import net.suqatri.cloud.node.NodeLauncher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
public class CloudServiceCopier implements ICloudServiceCopier {

    private final CloudServiceProcess process;
    private final ICloudServiceTemplateManager templateManager = this.templateManager;

    @Override
    public FutureAction<File> copyFilesAsync() {
        FutureAction<File> futureAction= new FutureAction<>();

        List<File> folders = new ArrayList<>();
        FutureActionCollection<String, IRBucketHolder<ICloudServiceTemplate>> futureActionCollection = new FutureActionCollection<>();

        this.templateManager.existsTemplateAsync("global-all")
            .onFailure(futureAction)
            .onSuccess(existsGlobal -> {
                if(existsGlobal) {
                    futureActionCollection.addToProcess("global-all", this.templateManager.getTemplateAsync("global-all"));
                }
                String globalEnvironmentTemplate = process.getServiceHolder().get().getEnvironment() == ServiceEnvironment.PROXY ? "global-proxy" : "global-minecraft";
                this.templateManager.existsTemplateAsync(globalEnvironmentTemplate)
                    .onFailure(futureAction)
                    .onSuccess(existsGlobalEnvironment -> {
                        if(existsGlobalEnvironment) {
                            futureActionCollection.addToProcess(globalEnvironmentTemplate, this.templateManager.getTemplateAsync(globalEnvironmentTemplate));
                        }
                        for (String templateName : process.getServiceHolder().get().getConfiguration().getTemplateNames()) {
                            futureActionCollection.addToProcess(templateName, this.templateManager.getTemplateAsync(templateName));
                        }
                        futureActionCollection.process()
                            .onFailure(futureAction)
                            .onSuccess(templates -> {
                                for (IRBucketHolder<ICloudServiceTemplate> templateHolder : templates.values()) {
                                    folders.add(templateHolder.get().getTemplateFolder());
                                }
                                this.process.getServiceHolder().get().getServiceVersion()
                                        .onFailure(futureAction)
                                        .onSuccess(serviceVersionHolder -> {
                                            CloudAPI.getInstance().getExecutorService().submit(() -> {
                                                try {
                                                    for (File folder : folders) {
                                                        if(!folder.exists()) continue;
                                                        FileUtils.copyDirectory(folder, this.getServiceDirectory());
                                                    }

                                                    List<File> configFiles = new ArrayList<>();

                                                    File pluginFolder = new File(getServiceDirectory(), "plugins");
                                                    if(!pluginFolder.exists()) pluginFolder.mkdirs();
                                                    switch (this.process.getServiceHolder().get().getEnvironment()){
                                                        case MINECRAFT:
                                                            FileUtils.copyFileToDirectory(Files.MINECRAFT_PLUGIN_JAR.getFile(), pluginFolder);
                                                            configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "bukkit.yml"));
                                                            configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "spigot.yml"));
                                                            configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "server.properties"));
                                                            break;

                                                        case PROXY:
                                                            FileUtils.copyFileToDirectory(Files.PROXY_PLUGIN_JAR.getFile(), pluginFolder);
                                                            configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "config.yml"));
                                                            break;
                                                    }

                                                    for (File configFile : configFiles) {
                                                        File target = new File(this.getServiceDirectory(), configFile.getName());
                                                        if(target.exists()) continue;
                                                        FileUtils.copyFileToDirectory(configFile, this.getServiceDirectory());
                                                    }

                                                    try {
                                                        editFiles();
                                                    }catch (IOException e) {
                                                        futureAction.completeExceptionally(e);
                                                        return;
                                                    }

                                                    FileUtils.copyFileToDirectory(serviceVersionHolder.get().getPatchedFile(), this.getServiceDirectory());

                                                    futureAction.complete(this.getServiceDirectory());
                                                } catch (IOException e) {
                                                    futureAction.completeExceptionally(e);
                                                }
                                            });
                                        });
                            });
                    });
            });

        return futureAction;
    }

    @Override
    public File copyFiles() throws Exception {
        List<File> folders = new ArrayList<>();

        IRBucketHolder<ICloudServiceVersion> serviceVersionHolder = this.process.getServiceHolder().get().getServiceVersion().get(5, TimeUnit.SECONDS);
        if(serviceVersionHolder == null) throw new NullPointerException("Service version " + this.process.getServiceHolder().get().getConfiguration().getServiceVersionName() + "not found");

        if(this.templateManager.existsTemplate("global-all")){
            folders.add(this.templateManager.getTemplate("global-all").get().getTemplateFolder());
        }

        String globalEnvironmentTemplate = process.getServiceHolder().get().getEnvironment() == ServiceEnvironment.PROXY ? "global-proxy" : "global-minecraft";
        if(this.templateManager.existsTemplate(globalEnvironmentTemplate)){
            folders.add(this.templateManager.getTemplate(globalEnvironmentTemplate).get().getTemplateFolder());
        }

        for (String templateName : process.getServiceHolder().get().getConfiguration().getTemplateNames()) {
            folders.add(this.templateManager.getTemplate(templateName).get().getTemplateFolder());
        }

        for (File folder : folders) {
            if(!folder.exists()) continue;
            FileUtils.copyDirectoryToDirectory(folder, getServiceDirectory());
        }

        List<File> configFiles = new ArrayList<>();

        File pluginFolder = new File(getServiceDirectory(), "plugins");
        if(!pluginFolder.exists()) pluginFolder.mkdirs();
        switch (this.process.getServiceHolder().get().getEnvironment()){
            case MINECRAFT:
                FileUtils.copyFileToDirectory(Files.MINECRAFT_PLUGIN_JAR.getFile(), pluginFolder);
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "bukkit.yml"));
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "spigot.yml"));
                File properties = new File(Files.STORAGE_FOLDER.getFile(), "server.properties");
                configFiles.add(properties);
                editProperties(properties);
                break;

            case PROXY:
                FileUtils.copyFileToDirectory(Files.PROXY_PLUGIN_JAR.getFile(), pluginFolder);
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "config.yml"));
                break;
        }

        for (File configFile : configFiles) {
            File target = new File(this.getServiceDirectory(), configFile.getName());
            if(target.exists()) continue;
            FileUtils.copyFileToDirectory(configFile, this.getServiceDirectory());
        }

        editFiles();

        FileUtils.copyFileToDirectory(serviceVersionHolder.get().getPatchedFile(), this.getServiceDirectory());

        return this.getServiceDirectory();
    }

    private void editFiles() throws IOException{
        switch (this.process.getServiceHolder().get().getEnvironment()){
            case MINECRAFT:
                editProperties(new File(this.getServiceDirectory(), "server.properties"));
                break;

            case PROXY:
                editConfig(new File(this.getServiceDirectory(), "config.yml"));
                break;
        }
    }

    private void editProperties(File properties) throws IOException{
        FileEditor fileEditor = new FileEditor(FileEditor.Type.PROPERTIES);
        fileEditor.read(properties);
        fileEditor.setValue("server-ip", NodeLauncher.getInstance().getNode().getHostname());
        fileEditor.setValue("server-port", String.valueOf(this.process.getPort()));
        fileEditor.setValue("max-players", String.valueOf(this.process.getServiceHolder().get().getMaxPlayers()));
        fileEditor.setValue("online-mode", "false");
        fileEditor.setValue("motd", this.process.getServiceHolder().get().getMotd());
        fileEditor.setValue("server-name", this.process.getServiceHolder().get().getServiceName());
        fileEditor.save(properties);
    }

    private void editConfig(File config) throws IOException{
        FileEditor fileEditor = new FileEditor(FileEditor.Type.YML);
        fileEditor.read(config);
        fileEditor.setValue("player_limit", String.valueOf(this.process.getServiceHolder().get().getMaxPlayers()));
        fileEditor.setValue("host", NodeLauncher.getInstance().getNode().getHostname() + ":" + this.process.getPort());
        fileEditor.save(config);
    }

    @Override
    public IRBucketHolder<ICloudService> getServiceHolder() {
        return this.process.getServiceHolder();
    }

    @Override
    public File getServiceDirectory() {
        return this.process.getServiceDirectory();
    }
}
