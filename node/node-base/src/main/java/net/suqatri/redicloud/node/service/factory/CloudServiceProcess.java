package net.suqatri.redicloud.node.service.factory;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.event.CloudServiceStoppedEvent;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.StreamUtils;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.ConsoleLine;
import net.suqatri.redicloud.node.service.NodeCloudServiceManager;
import net.suqatri.redicloud.node.service.screen.packet.ScreenDestroyPacket;
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
import java.util.stream.Collectors;

@Data
public class CloudServiceProcess implements ICloudServiceProcess {

    private final NodeCloudServiceFactory factory;
    private final IRBucketHolder<ICloudService> serviceHolder;
    private File serviceDirectory;
    private Process process;
    private int port;
    private Thread thread;
    private final FutureAction<Boolean> stopFuture = new FutureAction<>();
    private IServiceScreen screen;

    //TODO create packet for service
    @Override
    public void executeCommand(String command) {

    }

    @Override
    public boolean start() throws Exception {

        this.serviceDirectory = new File(this.serviceHolder.get().isStatic()
                ? Files.STATIC_SERVICE_FOLDER.getFile()
                : Files.TEMP_SERVICE_FOLDER.getFile(),
                this.serviceHolder.get().getServiceName() + "-" + this.serviceHolder.get().getUniqueId());
        if (!this.serviceDirectory.exists()) this.serviceDirectory.mkdirs();

        this.factory.getPortManager().getUnusedPort(this).get(5, TimeUnit.SECONDS);

        CloudServiceCopier copier = new CloudServiceCopier(this, CloudAPI.getInstance().getServiceTemplateManager());
        copier.copyFiles();

        CloudAPI.getInstance().getConsole().debug("Starting cloud service process " + this.serviceHolder.get().getServiceName() + " on port " + this.port);

        ProcessBuilder builder = new ProcessBuilder();
        Map<String, String> environment = builder.environment();
        environment.put("redicloud_service_id", this.getServiceHolder().get().getUniqueId().toString());
        environment.put("redicloud_path", NodeLauncher.getInstance().getNode().getFilePath());
        environment.put("redicloud_log_level", NodeLauncher.getInstance().getConsole().getLogLevel().name());
        for (Files value : Files.values()) {
            environment.put("redicloud_files_" + value.name().toLowerCase(), value.getFile().getAbsolutePath());
        }
        builder.directory(this.serviceDirectory);
        builder.command(getStartCommand(this.serviceHolder.get().getServiceVersion().get(3, TimeUnit.SECONDS)));
        CloudAPI.getInstance().getConsole().debug("Start command: " + builder.command().parallelStream().collect(Collectors.joining(" ")));
        this.process = builder.start();

        this.serviceHolder.get().setServiceState(ServiceState.STARTING);
        this.serviceHolder.getImpl(CloudService.class).setMaxRam(this.serviceHolder.get().getConfiguration().getMaxMemory());
        this.serviceHolder.getImpl(CloudService.class).setHostName(NodeLauncher.getInstance().getNode().getHostname());
        this.serviceHolder.getImpl(CloudService.class).setPort(this.port);
        this.serviceHolder.get().update();

        NodeLauncher.getInstance().getNode().setMemoryUsage(NodeLauncher.getInstance().getNode().getMemoryUsage()
                + this.serviceHolder.get().getConfiguration().getMaxMemory());
        NodeLauncher.getInstance().getNode().update();

        this.thread = new Thread(() -> {
            try {
                RateLimiter rate = RateLimiter.create(30, 5, TimeUnit.SECONDS);
                screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(this.serviceHolder);
                InputStreamReader inputStreamReader = new InputStreamReader(this.process.getInputStream());
                BufferedReader reader = new BufferedReader(inputStreamReader);
                while (
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
                    } catch (IOException e) {
                        //stream closed...
                    }
                }
                CloudAPI.getInstance().getConsole().trace("Closed stream for service " + this.serviceHolder.get().getServiceName());

                NodeLauncher.getInstance().getNode().setMemoryUsage(NodeLauncher.getInstance().getNode().getMemoryUsage()
                        - this.serviceHolder.get().getConfiguration().getMaxMemory());
                NodeLauncher.getInstance().getNode().update();

                reader.close();

                this.destroyScreen();

                this.factory.getPortManager().unUsePort(this);

                CloudAPI.getInstance().getEventManager().postGlobalAsync(new CloudServiceStoppedEvent(this.serviceHolder));

                if (!this.serviceHolder.get().isStatic())
                    ((NodeCloudServiceManager) this.factory.getServiceManager())
                            .deleteBucket(this.serviceHolder.get().getUniqueId().toString());

                CloudAPI.getInstance().getConsole().trace("Read error stream for service " + this.serviceHolder.get().getServiceName());
                if (StreamUtils.isOpen(this.process.getErrorStream())) {
                    reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
                    while (StreamUtils.isOpen(this.process.getErrorStream())) {
                        String line = reader.readLine();
                        if (line == null) continue;
                        CloudAPI.getInstance().getConsole().log(new ConsoleLine("SCREEN-ERROR [" + this.serviceHolder.get().getServiceName() + "]", line));
                    }
                    reader.close();
                }

                if (this.serviceDirectory.exists() && !this.serviceHolder.get().isStatic())
                    FileUtils.deleteDirectory(this.serviceDirectory);

                CloudAPI.getInstance().getConsole().debug("Cloud service process " + this.serviceHolder.get().getServiceName() + " has been stopped");

                CloudAPI.getInstance().getConsole().trace("Call stopping future action: " + this.stopFuture + " for service " + this.serviceHolder.get().getServiceName());
                if (!this.stopFuture.isFinishedAnyway()) {
                    this.stopFuture.complete(true);
                }

            } catch (Exception e) {

                this.stopFuture.completeExceptionally(e);
                CloudAPI.getInstance().getConsole().error("Cloud service process " + this.serviceHolder.get().getServiceName() + " has been stopped exceptionally!", e);

                this.destroyScreen();
                if (!this.serviceHolder.get().isStatic()) {
                    ((NodeCloudServiceManager) this.factory.getServiceManager()).deleteBucket(this.serviceHolder.get().getUniqueId().toString());
                    CloudAPI.getInstance().getServiceManager().removeFromFetcher(this.serviceHolder.get().getServiceName());
                } else {
                    this.serviceHolder.get().setServiceState(ServiceState.OFFLINE);
                    this.serviceHolder.get().updateAsync();
                }

                if (this.serviceDirectory.exists() && !this.serviceHolder.get().isStatic()) {
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

    private void destroyScreen() {
        if (this.screen == null) return;
        ScreenDestroyPacket screenDestroyPacket = null;
        for (UUID nodeId : this.serviceHolder.get().getConsoleNodeListenerIds()) {
            if (nodeId.equals(NodeLauncher.getInstance().getNode().getUniqueId())) continue;
            IRBucketHolder<ICloudNode> node = CloudAPI.getInstance().getNodeManager().getNode(nodeId);
            if (screenDestroyPacket == null) {
                screenDestroyPacket = new ScreenDestroyPacket();
                screenDestroyPacket.setServiceId(this.serviceHolder.get().getUniqueId());
            }
            screenDestroyPacket.getPacketData().addReceiver(node.get().getNetworkComponentInfo());
        }
        if (screenDestroyPacket != null) {
            screenDestroyPacket.publishAsync();
        }
        this.screen.deleteLines();
        this.screen = null;
    }

    @Override
    public FutureAction<Boolean> stopAsync(boolean force) {
        this.stopProcess(force);

        return this.stopFuture;
    }

    @Override
    public boolean isActive() {
        return this.process != null && this.process.isAlive();
    }

    public void deleteTempFiles(boolean force) throws IOException {
        if (isActive()) stopProcess(force);
        if (this.serviceHolder.get().isStatic() || !this.serviceDirectory.exists()) return;
        FileUtils.deleteDirectory(this.serviceDirectory);
    }

    public FutureAction<Boolean> deleteTempFilesAsync(boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if (isActive()) {
            stopProcess(force);
        }

        if (this.serviceHolder.get().isStatic() || !this.serviceDirectory.exists()) {
            futureAction.complete(true);
            return futureAction;
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

        this.factory.getPortManager().unUsePort(this);

        if (!isActive()) {
            if (!this.stopFuture.isFinishedAnyway()) {
                this.stopFuture.complete(true);
            }
            return;
        }
        if (force) {
            this.process.destroyForcibly();
        } else {
            this.process.destroy();
        }
    }

    private List<String> getStartCommand(IRBucketHolder<ICloudServiceVersion> serviceVersionHolder) {
        List<String> command = new ArrayList<>();

        command.add(serviceVersionHolder.get().getJavaCommand());

        command.addAll(this.serviceHolder.get().getConfiguration().getJvmArguments());


        command.add("-Xms" + this.serviceHolder.get().getConfiguration().getMaxMemory() + "M");
        command.add("-Xmx" + this.serviceHolder.get().getConfiguration().getMaxMemory() + "M");


        if (this.serviceHolder.get().getEnvironment() == ServiceEnvironment.MINECRAFT) {
            command.add("-Dcom.mojang.eula.agree=true");
            command.add("-Djline.terminal=jline.UnsupportedTerminal");
        }

        command.add("-jar");
        command.add(this.serviceDirectory.getAbsolutePath() + File.separator + "service.jar");

        if (this.serviceHolder.get().getEnvironment() == ServiceEnvironment.MINECRAFT) {
            command.add("nogui");
        }

        command.addAll(this.serviceHolder.get().getConfiguration().getProcessParameters());

        return command;
    }
}
