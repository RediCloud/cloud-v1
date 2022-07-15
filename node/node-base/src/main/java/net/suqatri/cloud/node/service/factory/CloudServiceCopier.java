package net.suqatri.cloud.node.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceCopier;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class CloudServiceCopier implements ICloudServiceCopier {

    private final CloudServiceServiceProcess process;
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
                                CloudAPI.getInstance().getExecutorService().submit(() -> {
                                    try {
                                        for (File folder : folders) {
                                            if(!folder.exists()) continue;
                                            FileUtils.copyDirectory(folder, this.getServiceDirectory());
                                        }

                                        File pluginFolder = new File(getServiceDirectory(), "plugins");
                                        if(!pluginFolder.exists()) pluginFolder.mkdirs();
                                        switch (this.process.getServiceHolder().get().getEnvironment()){
                                            case MINECRAFT:
                                                FileUtils.copyFileToDirectory(Files.MINECRAFT_PLUGIN_JAR.getFile(), pluginFolder);
                                                break;

                                            case PROXY:
                                                FileUtils.copyFileToDirectory(Files.PROXY_PLUGIN_JAR.getFile(), pluginFolder);
                                                break;
                                        }

                                        futureAction.complete(this.getServiceDirectory());
                                    } catch (IOException e) {
                                        futureAction.completeExceptionally(e);
                                    }
                                });
                            });
                    });
            });

        return futureAction;
    }

    @Override
    public File copyFiles() throws IOException {
        List<File> folders = new ArrayList<>();

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

        File pluginFolder = new File(getServiceDirectory(), "plugins");
        if(!pluginFolder.exists()) pluginFolder.mkdirs();
        switch (this.process.getServiceHolder().get().getEnvironment()){
            case MINECRAFT:
                FileUtils.copyFileToDirectory(Files.MINECRAFT_PLUGIN_JAR.getFile(), pluginFolder);
                break;

            case PROXY:
                FileUtils.copyFileToDirectory(Files.PROXY_PLUGIN_JAR.getFile(), pluginFolder);
                break;
        }

        return this.getServiceDirectory();
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
