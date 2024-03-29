package dev.redicloud.node.service.factory;

import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.node.service.factory.ICloudServiceCopier;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.version.ICloudServiceVersion;
import dev.redicloud.api.template.ICloudServiceTemplate;
import dev.redicloud.api.template.ICloudServiceTemplateManager;
import dev.redicloud.api.utils.Files;
import dev.redicloud.commons.file.FileEditor;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.commons.function.future.FutureActionCollection;
import dev.redicloud.node.NodeLauncher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
public class CloudServiceCopier implements ICloudServiceCopier {

    private final CloudServiceProcess process;
    private final ICloudServiceTemplateManager templateManager;

    @Override
    public FutureAction<File> copyFilesAsync() {
        FutureAction<File> futureAction = new FutureAction<>();

        List<File> folders = new ArrayList<>();
        FutureActionCollection<String, ICloudServiceTemplate> futureActionCollection = new FutureActionCollection<>();

        this.templateManager.existsTemplateAsync("global-all")
                .onFailure(futureAction)
                .onSuccess(existsGlobal -> {
                    if (existsGlobal) {
                        futureActionCollection.addToProcess("global-all", this.templateManager.getTemplateAsync("global-all"));
                    }

                    String globalEnvironmentTemplate = "";
                    switch (this.process.getService().getEnvironment()){
                        case LIMBO:
                            globalEnvironmentTemplate = "global-limbo";
                            break;
                        case MINECRAFT:
                            globalEnvironmentTemplate = "global-minecraft";
                            break;
                        case BUNGEECORD:
                            globalEnvironmentTemplate = "global-bungeecord";
                            break;
                        case VELOCITY:
                            globalEnvironmentTemplate = "global-velocity";
                            break;
                    }

                    String finalGlobalEnvironmentTemplate = globalEnvironmentTemplate;
                    this.templateManager.existsTemplateAsync(globalEnvironmentTemplate)
                            .onFailure(futureAction)
                            .onSuccess(existsGlobalEnvironment -> {
                                if (existsGlobalEnvironment) {
                                    futureActionCollection.addToProcess(finalGlobalEnvironmentTemplate, this.templateManager.getTemplateAsync(finalGlobalEnvironmentTemplate));
                                }
                                for (String templateName : process.getService().getConfiguration().getTemplateNames()) {
                                    futureActionCollection.addToProcess(templateName, this.templateManager.getTemplateAsync(templateName));
                                }
                                futureActionCollection.process()
                                        .onFailure(futureAction)
                                        .onSuccess(templates -> {
                                            for (ICloudServiceTemplate template : templates.values()) {
                                                folders.add(template.getTemplateFolder());
                                            }
                                            this.process.getService().getServiceVersion()
                                                    .onFailure(futureAction)
                                                    .onSuccess(serviceVersion -> {
                                                        CloudAPI.getInstance().getExecutorService().submit(() -> {
                                                            try {
                                                                for (File folder : folders) {
                                                                    if (!folder.exists()) continue;
                                                                    for (File file : folder.listFiles()) {
                                                                        if(file.isDirectory()){
                                                                            FileUtils.copyDirectoryToDirectory(file, this.getServiceDirectory());
                                                                        }else{
                                                                            FileUtils.copyFileToDirectory(file, this.getServiceDirectory());
                                                                        }
                                                                    }
                                                                }

                                                                List<File> configFiles = new ArrayList<>();

                                                                File pluginFolder = new File(getServiceDirectory(), "plugins");
                                                                if (!pluginFolder.exists()) pluginFolder.mkdirs();
                                                                switch (this.process.getService().getEnvironment()) {
                                                                    case MINECRAFT:
                                                                        FileUtils.copyFileToDirectory(Files.MINECRAFT_PLUGIN_JAR.getFile(), pluginFolder);
                                                                        configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "bukkit.yml"));
                                                                        configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "spigot.yml"));
                                                                        configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "server.properties"));
                                                                        break;
                                                                    case LIMBO:
                                                                        configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "limbo.yml"));
                                                                        break;
                                                                    case BUNGEECORD:
                                                                        FileUtils.copyFileToDirectory(Files.BUNGEECORD_PLUGIN_JAR.getFile(), pluginFolder);
                                                                        configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "config.yml"));
                                                                        break;
                                                                    case VELOCITY:
                                                                        FileUtils.copyFileToDirectory(Files.VELOCITY_PLUGIN_JAR.getFile(), pluginFolder);
                                                                        configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "velocity.toml"));
                                                                        break;
                                                                }

                                                                if (Files.SERVER_ICON.exists())
                                                                    FileUtils.copyFileToDirectory(Files.SERVER_ICON.getFile(), getServiceDirectory());

                                                                for (File configFile : configFiles) {
                                                                    File target = new File(this.getServiceDirectory(), configFile.getName());
                                                                    if (target.exists()) continue;
                                                                    FileUtils.copyFileToDirectory(configFile, this.getServiceDirectory());
                                                                }

                                                                try {
                                                                    editFiles();
                                                                } catch (IOException e) {
                                                                    futureAction.completeExceptionally(e);
                                                                    return;
                                                                }

                                                                FileUtils.copyFile(serviceVersion.getPatchedFile(), new File(this.getServiceDirectory(), "service.jar"));

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

        CloudAPI.getInstance().getConsole().debug("Copying files for service " + this.process.getService().getName() + "...");

        ICloudServiceVersion serviceVersion = this.process.getService().getServiceVersion().get(5, TimeUnit.SECONDS);
        if (serviceVersion == null)
            throw new NullPointerException("Service version " + this.process.getService().getConfiguration().getServiceVersionName() + "not found");

        if (this.templateManager.existsTemplate("global-all")) {
            folders.add(this.templateManager.getTemplate("global-all").getTemplateFolder());
        }

        String globalEnvironmentTemplate = "";
        switch (this.process.getService().getEnvironment()){
            case LIMBO:
                globalEnvironmentTemplate = "global-limbo";
                break;
            case MINECRAFT:
                globalEnvironmentTemplate = "global-minecraft";
                break;
            case BUNGEECORD:
                globalEnvironmentTemplate = "global-bungeecord";
                break;
            case VELOCITY:
                globalEnvironmentTemplate = "global-velocity";
                break;
        }
        if (this.templateManager.existsTemplate(globalEnvironmentTemplate)) {
            folders.add(this.templateManager.getTemplate(globalEnvironmentTemplate).getTemplateFolder());
        }

        for (String templateName : process.getService().getConfiguration().getTemplateNames()) {
            folders.add(this.templateManager.getTemplate(templateName).getTemplateFolder());
        }

        for (File folder : folders) {
            if (!folder.exists()) continue;
            for (File file : folder.listFiles()) {
                if(file.isDirectory()){
                    FileUtils.copyDirectoryToDirectory(file, this.getServiceDirectory());
                }else{
                    FileUtils.copyFileToDirectory(file, this.getServiceDirectory());
                }
            }
        }

        List<File> configFiles = new ArrayList<>();

        File pluginFolder = new File(getServiceDirectory(), "plugins");
        if (!pluginFolder.exists()) pluginFolder.mkdirs();
        switch (this.process.getService().getEnvironment()) {
            case MINECRAFT:
                FileUtils.copyFileToDirectory(Files.MINECRAFT_PLUGIN_JAR.getFile(), pluginFolder);
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "bukkit.yml"));
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "spigot.yml"));
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "server.properties"));
                break;

            case BUNGEECORD:
                FileUtils.copyFileToDirectory(Files.BUNGEECORD_PLUGIN_JAR.getFile(), pluginFolder);
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "config.yml"));
                break;
            case VELOCITY:
                FileUtils.copyFileToDirectory(Files.VELOCITY_PLUGIN_JAR.getFile(), pluginFolder);
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "velocity.toml"));
                break;
            case LIMBO:
                configFiles.add(new File(Files.STORAGE_FOLDER.getFile(), "limbo.yml"));
                break;
        }

        if (Files.SERVER_ICON.exists())
            FileUtils.copyFileToDirectory(Files.SERVER_ICON.getFile(), getServiceDirectory());

        for (File configFile : configFiles) {
            File target = new File(this.getServiceDirectory(), configFile.getName());
            if (target.exists()) continue;
            FileUtils.copyFileToDirectory(configFile, this.getServiceDirectory());
        }

        editFiles();

        FileUtils.copyFile(serviceVersion.getPatchedFile(), new File(this.getServiceDirectory(), "service.jar"));

        CloudAPI.getInstance().getConsole().debug("Copying files for service " + this.process.getService().getName() + " finished.");

        return this.getServiceDirectory();
    }

    private void editFiles() throws IOException {
        switch (this.process.getService().getEnvironment()) {
            case MINECRAFT:
                editProperties(new File(this.getServiceDirectory(), "server.properties"));
                break;

            case BUNGEECORD:
                editConfig(new File(this.getServiceDirectory(), "config.yml"));
                break;
            case VELOCITY:
                editVelocity(new File(this.getServiceDirectory(), "velocity.toml"));
                break;
            case LIMBO:
                editLimbo(new File(this.getServiceDirectory(), "limbo.yml"));
                break;
        }
    }

    private void editLimbo(File limboFile) throws IOException {
        FileEditor fileEditor = new FileEditor(FileEditor.Type.YML);
        fileEditor.read(limboFile);
        fileEditor.setValue("ip", "''");
        fileEditor.setValue("port", String.valueOf(this.process.getPort()));
        fileEditor.save(limboFile);
    }

    private void editProperties(File properties) throws IOException {
        FileEditor fileEditor = new FileEditor(FileEditor.Type.PROPERTIES);
        fileEditor.read(properties);
        fileEditor.setValue("server-ip", NodeLauncher.getInstance().getNode().getHostname());
        fileEditor.setValue("server-port", String.valueOf(this.process.getPort()));
        fileEditor.setValue("max-players", String.valueOf(this.process.getService().getMaxPlayers()));
        fileEditor.setValue("online-mode", "false");
        fileEditor.setValue("motd", this.process.getService().getMotd());
        fileEditor.setValue("server-name", this.process.getService().getServiceName());
        fileEditor.save(properties);
    }

    private void editVelocity(File config) throws IOException {
        FileEditor fileEditor = new FileEditor(FileEditor.Type.TOML);
        fileEditor.read(config);
        fileEditor.setValue("bind", "\"0.0.0.0:" + this.process.getPort() + "\"");
        fileEditor.setValue("show-max-players", String.valueOf(this.process.getService().getMaxPlayers()));
        fileEditor.save(config);
    }

    private void editConfig(File config) throws IOException {
        FileEditor fileEditor = new FileEditor(FileEditor.Type.YML);
        fileEditor.read(config);
        fileEditor.setValue("player_limit", String.valueOf(this.process.getService().getMaxPlayers()));
        fileEditor.setValue("max_players", String.valueOf(this.process.getService().getMaxPlayers()));
        fileEditor.setValue("host", NodeLauncher.getInstance().getNode().getHostname() + ":" + this.process.getPort());
        fileEditor.save(config);
    }

    @Override
    public ICloudService getServices() {
        return this.process.getService();
    }

    @Override
    public File getServiceDirectory() {
        return this.process.getServiceDirectory();
    }
}
