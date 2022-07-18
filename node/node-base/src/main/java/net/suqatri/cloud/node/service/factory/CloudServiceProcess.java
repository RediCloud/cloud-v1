package net.suqatri.cloud.node.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.cloud.api.node.service.screen.IServiceScreen;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.console.ConsoleLine;
import net.suqatri.cloud.node.service.NodeCloudServiceManager;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
public class CloudServiceProcess implements ICloudServiceProcess {

    private final NodeCloudServiceFactory factory;
    private final IRBucketHolder<ICloudService> serviceHolder;
    private File serviceDirectory;
    private Process process;
    private int port;

    //TODO create packet for service
    @Override
    public void executeCommand(String command) {

    }

    @Override
    public boolean start() throws Exception {

        this.serviceDirectory = new File(Files.TEMP_SERVICE_FOLDER.getFile(), this.serviceHolder.get().getServiceName() + "-" + this.serviceHolder.get().getUniqueId());
        this.serviceDirectory.mkdirs();

        this.factory.getPortManager().getUnusedPort(this).get(5, TimeUnit.SECONDS);

        CloudServiceCopier copier = new CloudServiceCopier(this, CloudAPI.getInstance().getServiceTemplateManager());
        copier.copyFiles();

        CloudAPI.getInstance().getConsole().debug("Starting cloud service process " + this.serviceHolder.get().getServiceName() + " on port " + this.port);

        ProcessBuilder builder = new ProcessBuilder();
        Map<String, String> environment = builder.environment();
        environment.put("serviceId", this.getServiceHolder().get().getUniqueId().toString());
        builder.directory(this.serviceDirectory);
        builder.command(getStartCommand());
        this.process = builder.start();

        new Thread(() -> {
            IServiceScreen screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(this.serviceHolder);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
                while(this.process.isAlive()){
                    String line = reader.readLine();
                    if(line == null) continue;
                    if(line.isEmpty() || line.equals(" ") || line.contains("InitialHandler has pinged")) continue; //"InitialHandler has pinged" for ping flood protection
                    screen.addLine(line);
                }
                ((NodeCloudServiceManager)this.factory.getServiceManager()).deleteBucket(this.serviceHolder.get().getUniqueId().toString());
                reader.close();
                reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
                while(reader.ready()){ //TODO: print error remotely to all nodes
                    String line = reader.readLine();
                    CloudAPI.getInstance().getConsole().log(new ConsoleLine("SCREEN-ERROR [" + this.serviceHolder.get().getServiceName() + "]", line));
                }
                reader.close();
                FileUtils.deleteDirectory(this.serviceDirectory);
                CloudAPI.getInstance().getConsole().debug("Cloud service process " + this.serviceHolder.get().getServiceName() + " has been stopped");
            } catch (IOException e) {
                ((NodeCloudServiceManager)this.factory.getServiceManager()).deleteBucket(this.serviceHolder.get().getUniqueId().toString());
                CloudAPI.getInstance().getConsole().error("Cloud service process " + this.serviceHolder.get().getServiceName() + " has been stopped exceptionally!", e);
                if(this.serviceDirectory.exists()){
                    CloudAPI.getInstance().getConsole().warn("Temp service directory of " + this.serviceHolder.get().getServiceName() + "cannot be deleted (" + this.serviceDirectory.getAbsolutePath() + ")");
                }
            }
        }, "redicloud-service-" + this.serviceHolder.get().getServiceName()).start();

        this.serviceHolder.get().setServiceState(ServiceState.STARTING);
        this.serviceHolder.get().update();

        return true;
    }

    @Override
    public FutureAction<Boolean> startAsync() {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        this.serviceDirectory = new File(Files.TEMP_SERVICE_FOLDER.getFile(), this.serviceHolder.get().getServiceName() + "-" + this.serviceHolder.get().getUniqueId().toString());
        this.serviceDirectory.mkdirs();

        CloudServiceCopier copier = new CloudServiceCopier(this, CloudAPI.getInstance().getServiceTemplateManager());
        copier.copyFilesAsync()
                .onFailure(futureAction)
                .onSuccess(f -> {
                    this.factory.getPortManager().getUnusedPort(this)
                        .onFailure(futureAction)
                        .onSuccess(port -> {
                            this.port = port;
                            try {
                                ProcessBuilder builder = new ProcessBuilder();
                                Map<String, String> environment = builder.environment();
                                environment.put("serviceId", this.getServiceHolder().get().getUniqueId().toString());
                                builder.command(getStartCommand());
                                this.process = builder.start();

                                this.serviceHolder.get().setServiceState(ServiceState.STARTING);
                                this.serviceHolder.get().updateAsync();

                                futureAction.complete(true);
                            }catch (Exception e){
                                futureAction.completeExceptionally(e);
                            }
                        });
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
                this.factory.getThread().getProcesses().remove(this.serviceHolder.get().getUniqueId());
                futureAction.complete(true);
            });

        return futureAction;
    }

    @Override
    public boolean stop(boolean force) throws IOException {
        if(isActive()) this.stopProcess(force);

        deleteTempFiles(force);
        this.factory.getThread().getProcesses().remove(this.serviceHolder.get().getUniqueId());

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

        command.addAll(this.serviceHolder.get().getConfiguration().getJvmArguments());


        command.add("-Xms" + this.serviceHolder.get().getConfiguration().getMaxMemory() + "M");
        command.add("-Xmx" + this.serviceHolder.get().getConfiguration().getMaxMemory() + "M");


        if(this.serviceHolder.get().getEnvironment() == ServiceEnvironment.MINECRAFT){
            command.add("-Dcom.mojang.eula.agree=true");
            command.add("-Djline.terminal=jline.UnsupportedTerminal");
        }

        command.add("-jar");
        command.add(this.serviceDirectory.getAbsolutePath() + File.separator + "service.jar");

        if(this.serviceHolder.get().getEnvironment() == ServiceEnvironment.MINECRAFT){
            command.add("nogui");
        }

        command.addAll(this.serviceHolder.get().getConfiguration().getProcessParameters());

        return command;
    }
}
