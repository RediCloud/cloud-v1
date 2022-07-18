package net.suqatri.cloud.node.service.factory;

import com.google.common.util.concurrent.RateLimiter;
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
    private Thread thread;
    private FutureAction<Boolean> stopFuture;

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

        this.thread = new Thread(() -> {
            try {
                RateLimiter rate = RateLimiter.create(15, 5, TimeUnit.SECONDS);
                IServiceScreen screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(this.serviceHolder);
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
                while(this.process.isAlive() && Thread.currentThread().isAlive() && !Thread.currentThread().isInterrupted()) {
                    String line = reader.readLine();
                    if(line == null) continue;
                    if(line.isEmpty() || line.equals(" ") || line.contains("InitialHandler has pinged")) continue; //"InitialHandler has pinged" for ping flood protection
                    rate.acquire();
                    screen.addLine(line);
                }
                ((NodeCloudServiceManager)this.factory.getServiceManager()).deleteBucket(this.serviceHolder.get().getUniqueId().toString());
                screen.delete();
                reader.close();
                reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
                while(reader.ready()){ //TODO: print error remotely to all nodes
                    String line = reader.readLine();
                    CloudAPI.getInstance().getConsole().log(new ConsoleLine("SCREEN-ERROR [" + this.serviceHolder.get().getServiceName() + "]", line));
                }
                reader.close();
                if(this.serviceDirectory.exists()) FileUtils.deleteDirectory(this.serviceDirectory);
                CloudAPI.getInstance().getConsole().debug("Cloud service process " + this.serviceHolder.get().getServiceName() + " has been stopped");
                if(this.stopFuture != null) this.stopFuture.complete(true);
            } catch (IOException e) {
                if(this.stopFuture != null) this.stopFuture.completeExceptionally(e);
                ((NodeCloudServiceManager)this.factory.getServiceManager()).deleteBucket(this.serviceHolder.get().getUniqueId().toString());
                CloudAPI.getInstance().getConsole().error("Cloud service process " + this.serviceHolder.get().getServiceName() + " has been stopped exceptionally!", e);
                if(this.serviceDirectory.exists()){
                    CloudAPI.getInstance().getConsole().warn("Temp service directory of " + this.serviceHolder.get().getServiceName() + "cannot be deleted (" + this.serviceDirectory.getAbsolutePath() + ")");
                }
            }
        }, "redicloud-service-" + this.serviceHolder.get().getServiceName());
        this.thread.start();

        this.serviceHolder.get().setServiceState(ServiceState.STARTING);
        this.serviceHolder.get().update();

        return true;
    }

    @Override
    public FutureAction<Boolean> stopAsync(boolean force) {
        FutureAction<Boolean> futureAction = this.stopFuture != null ? this.stopFuture : new FutureAction<>();

        this.stopFuture = futureAction;

        if(isActive()) this.stopProcess(force);

        return futureAction;
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
