package net.suqatri.cloud.node.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class CloudServiceServiceProcess implements ICloudServiceProcess {

    private final NodeCloudServiceFactory factory;
    private final IRBucketHolder<ICloudService> serviceHolder;
    private File serviceDirectory;
    private Process process;

    //TODO create packet for service
    @Override
    public void executeCommand(String command) {

    }

    @Override
    public boolean start() throws Exception {

        this.serviceDirectory = new File(Files.TEMP_SERVICE_FOLDER.getFile(), this.serviceHolder.get().getServiceName() + "-" + this.serviceHolder.get().getUniqueId().toString());
        this.serviceDirectory.mkdirs();

        CloudServiceCopier copier = new CloudServiceCopier(this);
        copier.copyFiles();

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(getStartCommand());
        this.process = builder.start();

        this.serviceHolder.get().setServiceState(ServiceState.STARTING);
        this.serviceHolder.get().update();

        return true;
    }

    @Override
    public FutureAction<Boolean> startAsync() {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        this.serviceDirectory = new File(Files.TEMP_SERVICE_FOLDER.getFile(), this.serviceHolder.get().getServiceName() + "-" + this.serviceHolder.get().getUniqueId().toString());
        this.serviceDirectory.mkdirs();

        CloudServiceCopier copier = new CloudServiceCopier(this);
        copier.copyFilesAsync()
                .onFailure(futureAction)
                .onSuccess(f -> {
                    try {
                        ProcessBuilder builder = new ProcessBuilder();
                        builder.command(getStartCommand());
                        this.process = builder.start();

                        this.serviceHolder.get().setServiceState(ServiceState.STARTING);
                        this.serviceHolder.get().updateAsync();

                        futureAction.complete(true);
                    }catch (Exception e){
                        futureAction.completeExceptionally(e);
                    }
                });

        return futureAction;
    }

    @Override
    public FutureAction<Boolean> stopAsync(boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if(isActive()) this.stopProcess(force);

        deleteTempFilesAsync(force)
            .onFailure(futureAction)
            .onSuccess(b -> {
                this.factory.getProcesses().remove(this.serviceHolder.get().getUniqueId());
               futureAction.complete(true);
            });

        return futureAction;
    }

    @Override
    public boolean stop(boolean force) throws IOException {
        if(isActive()) this.stopProcess(force);

        deleteTempFiles(force);
        this.factory.getProcesses().remove(this.serviceHolder.get().getUniqueId());

        return true;
    }

    @Override
    public boolean isActive() {
        return this.process != null && this.process.isAlive();
    }

    public void deleteTempFiles(boolean force) throws IOException {
        if(isActive()) stopProcess(force);
        FileUtils.deleteDirectory(this.serviceDirectory);
    }

    public FutureAction<Boolean> deleteTempFilesAsync(boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if(isActive()){
            stopProcess(force);
        }

        CloudAPI.getInstance().getExecutorService().submit(() -> {
            try {
                FileUtils.deleteDirectory(this.serviceDirectory);
                futureAction.complete(true);
            } catch (IOException e) {
                futureAction.completeExceptionally(e);
            }
        });

        return futureAction;
    }

    public void stopProcess(boolean force) {
        if(!isActive()) return;
        if(force){
            this.process.destroyForcibly();
        }else {
            this.process.destroy();
        }
    }

    private List<String> getStartCommand(){
        List<String> command = new ArrayList<>();

        command.add(this.serviceHolder.get().getConfiguration().getJavaCommand());
        command.addAll(this.serviceHolder.get().getConfiguration().getProcessParameters());
        command.add("-Xms" + this.serviceHolder.get().getConfiguration().getMaxMemory() + "M");
        command.add("-Xmx" + this.serviceHolder.get().getConfiguration().getMaxMemory() + "M");
        command.add("-Dcom.mojang.eula.agree=true");
        command.add("-Djline.terminal=jline.UnsupportedTerminal");
        command.add(this.serviceDirectory.getAbsolutePath() + File.separator + "service.jar");
        command.addAll(this.serviceHolder.get().getConfiguration().getJvmArguments());

        if(this.serviceHolder.get().getEnvironment() == ServiceEnvironment.MINECRAFT){
            command.add("nogui");
        }

        return command;
    }
}
