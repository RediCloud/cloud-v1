package net.suqatri.cloud.node.service.factory;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.cloud.api.node.service.screen.IServiceScreen;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.StreamUtils;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.console.ConsoleLine;
import net.suqatri.cloud.node.service.NodeCloudServiceManager;
import net.suqatri.cloud.node.service.screen.packet.ScreenDestroyPacket;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private IServiceScreen screen;

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
        environment.put("redicloud_serviceId", this.getServiceHolder().get().getUniqueId().toString());
        environment.put("redicloud_redis_path", Files.REDIS_CONFIG.getFile().getAbsolutePath());
        builder.directory(this.serviceDirectory);
        builder.command(getStartCommand());
        this.process = builder.start();

        this.serviceHolder.get().setServiceState(ServiceState.STARTING);
        this.serviceHolder.getImpl(CloudService.class).setMaxRam(this.serviceHolder.get().getConfiguration().getMaxMemory());
        this.serviceHolder.getImpl(CloudService.class).setHostName(NodeLauncher.getInstance().getNode().getHostname());
        this.serviceHolder.getImpl(CloudService.class).setPort(this.port);
        this.serviceHolder.get().update();

        this.thread = new Thread(() -> {
            try {
                RateLimiter rate = RateLimiter.create(15, 5, TimeUnit.SECONDS);
                screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(this.serviceHolder);
                InputStreamReader inputStreamReader = new InputStreamReader(this.process.getInputStream());
                BufferedReader reader = new BufferedReader(inputStreamReader);
                while(
                        this.process.isAlive()
                        && Thread.currentThread().isAlive()
                        && !Thread.currentThread().isInterrupted()
                        && StreamUtils.isOpen(this.process.getInputStream())
                ) {
                    try {
                        String line = reader.readLine();
                        if (line == null) continue;
                        if (line.isEmpty() || line.equals(" ") || line.contains("InitialHandler has pinged"))
                            continue; //"InitialHandler has pinged" for ping flood protection
                        rate.acquire();
                        screen.addLine(line);
                    }catch (IOException e) {
                        //stream closed...
                    }
                }
                this.factory.getPortManager().unusePort(this.port);
                reader.close();

                this.destroyScreen();
                ((NodeCloudServiceManager)this.factory.getServiceManager()).deleteBucket(this.serviceHolder.get().getUniqueId().toString());



                if(StreamUtils.isOpen(this.process.getErrorStream())) {
                    reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
                    while (reader.ready()) {
                        String line = reader.readLine();
                        CloudAPI.getInstance().getConsole().log(new ConsoleLine("SCREEN-ERROR [" + this.serviceHolder.get().getServiceName() + "]", line));
                    }
                    reader.close();
                }

                if(this.serviceDirectory.exists()) FileUtils.deleteDirectory(this.serviceDirectory);

                CloudAPI.getInstance().getConsole().debug("Cloud service process " + this.serviceHolder.get().getServiceName() + " has been stopped");

                if(this.stopFuture != null) this.stopFuture.complete(true);

            } catch (Exception e) {

                if (this.stopFuture != null) {
                    this.stopFuture.completeExceptionally(e);
                }else{
                    CloudAPI.getInstance().getConsole().error("Cloud service process " + this.serviceHolder.get().getServiceName() + " has been stopped exceptionally!", e);
                }

                this.destroyScreen();
                ((NodeCloudServiceManager) this.factory.getServiceManager()).deleteBucket(this.serviceHolder.get().getUniqueId().toString());

                if (this.serviceDirectory.exists()) {
                    try {
                        FileUtils.deleteDirectory(this.serviceDirectory);
                    } catch (IOException e1) {
                        CloudAPI.getInstance().getConsole().error("Temp service directory of " + this.serviceHolder.get().getServiceName() + " cannot be deleted (" + this.serviceDirectory.getAbsolutePath() + ")", e1);
                    }
                }

            }
        }, "redicloud-service-" + this.serviceHolder.get().getServiceName());
        this.thread.start();

        return true;
    }

    private void destroyScreen(){
        if(this.screen == null) return;
        ScreenDestroyPacket screenDestroyPacket = null;
        for (UUID nodeId : this.serviceHolder.get().getConsoleNodeListenerIds()) {
            if(nodeId.equals(NodeLauncher.getInstance().getNode().getUniqueId())) continue;
            IRBucketHolder<ICloudNode> node = CloudAPI.getInstance().getNodeManager().getNode(nodeId);
            if(screenDestroyPacket == null) {
                screenDestroyPacket = new ScreenDestroyPacket();
                screenDestroyPacket.setServiceId(this.serviceHolder.get().getUniqueId());
            }
            screenDestroyPacket.getPacketData().addReceiver(node.get().getNetworkComponentInfo());
        }
        if(screenDestroyPacket != null){
            screenDestroyPacket.publishAsync();
        }
        this.screen.deleteLines();
        this.screen = null;
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

        this.factory.getPortManager().unusePort(this.port);

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
