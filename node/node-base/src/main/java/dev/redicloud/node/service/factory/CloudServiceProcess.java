package dev.redicloud.node.service.factory;

import com.google.common.util.concurrent.RateLimiter;
import dev.redicloud.node.service.screen.packet.ScreenDestroyPacket;
import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.service.CloudService;
import dev.redicloud.api.impl.service.packet.stop.CloudServiceInitStopPacket;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.node.service.factory.ICloudServiceProcess;
import dev.redicloud.api.node.service.screen.IServiceScreen;
import dev.redicloud.api.packet.PacketChannel;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.service.event.CloudServiceStoppedEvent;
import dev.redicloud.api.service.version.ICloudServiceVersion;
import dev.redicloud.api.utils.Files;
import dev.redicloud.commons.StreamUtils;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.node.NodeLauncher;
import dev.redicloud.node.console.ConsoleLine;
import dev.redicloud.node.service.NodeCloudServiceManager;
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
    private final CloudService service;
    private File serviceDirectory;
    private Process process;
    private int port;
    private Thread thread;
    private final FutureAction<Boolean> stopFuture = new FutureAction<>();
    private IServiceScreen screen;

    @Override
    public void executeCommand(String command) {
        CloudAPI.getInstance().getServiceManager().executeCommand(this.service, command);
    }

    @Override
    public boolean start() throws Exception {

        this.serviceDirectory = new File(this.service.isStatic()
                ? Files.STATIC_SERVICE_FOLDER.getFile()
                : Files.TEMP_SERVICE_FOLDER.getFile(),
                this.service.getServiceName() + "-" + this.service.getUniqueId());
        if (!this.serviceDirectory.exists()) this.serviceDirectory.mkdirs();

        try {
            this.factory.getPortManager().getUnusedPort(this).get(10, TimeUnit.SECONDS);
        }catch (Exception e){
            CloudAPI.getInstance().getConsole().fatal("Could not get unused port for service " + this.service.getServiceName(), e);
            this.stopProcess(true);
            return false;
        }

        try {
            CloudServiceCopier copier = new CloudServiceCopier(this, CloudAPI.getInstance().getServiceTemplateManager());
            copier.copyFiles();
        }catch (Exception e){
            CloudAPI.getInstance().getConsole().fatal("Could not copy service files for service " + this.service.getServiceName(), e);
            this.stopProcess(true);
            return false;
        }

        CloudAPI.getInstance().getConsole().debug("Starting cloud service process " + this.service.getServiceName() + " on port " + this.port);

        ProcessBuilder builder = new ProcessBuilder();
        Map<String, String> environment = builder.environment();
        environment.put("redicloud_service_id", this.getService().getUniqueId().toString());
        environment.put("redicloud_path", NodeLauncher.getInstance().getNode().getFilePath());
        environment.put("redicloud_log_level", NodeLauncher.getInstance().getConsole().getLogLevel().name());
        for (Files value : Files.values()) {
            environment.put("redicloud_files_" + value.name().toLowerCase(), value.getFile().toPath().toAbsolutePath().toString());
        }
        builder.directory(this.serviceDirectory);
        builder.command(getStartCommand(this.service.getServiceVersion().get(3, TimeUnit.SECONDS)));
        CloudAPI.getInstance().getConsole().debug("Start command: " + builder.command().parallelStream().collect(Collectors.joining(" ")));
        this.process = builder.start();

        this.service.setServiceState(ServiceState.STARTING);
        this.service.setMaxRam(this.service.getConfiguration().getMaxMemory());
        this.service.setHostName(NodeLauncher.getInstance().getNode().getHostname());
        this.service.setPort(this.port);
        this.service.setLastPlayerAction(System.currentTimeMillis());
        this.service.update();

        NodeLauncher.getInstance().getNode().setMemoryUsage(NodeLauncher.getInstance().getNode().getMemoryUsage()
                + this.service.getConfiguration().getMaxMemory());
        NodeLauncher.getInstance().getNode().update();

        this.thread = new Thread(() -> {
            try {
                RateLimiter rate = RateLimiter.create(30, 5, TimeUnit.SECONDS);
                screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(this.service);
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
                CloudAPI.getInstance().getConsole().trace("Closed stream for service " + this.service.getServiceName());

                NodeLauncher.getInstance().getNode().setMemoryUsage(NodeLauncher.getInstance().getNode().getMemoryUsage()
                        - this.service.getConfiguration().getMaxMemory());
                NodeLauncher.getInstance().getNode().update();

                reader.close();

                this.destroyScreen();

                this.factory.getPortManager().unUsePort(this);

                CloudAPI.getInstance().getEventManager().postGlobalAsync(new CloudServiceStoppedEvent(this.service));

                if (!this.service.isStatic())
                    this.factory.getServiceManager()
                            .deleteBucket(this.service);

                if (StreamUtils.isOpen(this.process.getErrorStream())) {
                    CloudAPI.getInstance().getConsole().trace("Read error stream for service " + this.service.getServiceName());
                    reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
                    while (
                            StreamUtils.isOpen(this.process.getErrorStream())
                            && Thread.currentThread().isAlive()
                            && !Thread.currentThread().isInterrupted()
                            && reader.ready()
                    ) {
                        String line = reader.readLine();
                        if (line == null) continue;
                        CloudAPI.getInstance().getConsole().log(new ConsoleLine("SCREEN-ERROR [" + this.service.getServiceName() + "]", line));
                    }
                    CloudAPI.getInstance().getConsole().trace("Closed error stream for service " + this.service.getServiceName());
                    reader.close();
                }

                if (this.serviceDirectory.exists() && !this.service.isStatic()) {
                    FileUtils.deleteDirectory(this.serviceDirectory);
                }
                CloudAPI.getInstance().getConsole().debug("Cloud service process " + this.service.getServiceName() + " has been stopped");

                CloudAPI.getInstance().getConsole().trace("Call stopping future action: " + this.stopFuture + " for service " + this.service.getServiceName());
                if (!this.stopFuture.isFinishedAnyway()) {
                    this.stopFuture.complete(true);
                }

            } catch (Exception e) {

                this.stopFuture.completeExceptionally(e);
                CloudAPI.getInstance().getConsole().error("Cloud service process " + this.service.getServiceName() + " has been stopped exceptionally!", e);

                this.destroyScreen();
                this.factory.getPortManager().unUsePort(this);
                if (!this.service.isStatic()) {
                    this.factory.getServiceManager().deleteBucket(this.service);
                } else {
                    this.service.setServiceState(ServiceState.OFFLINE);
                    this.service.updateAsync();
                }

                if (this.serviceDirectory.exists() && !this.service.isStatic()) {
                    try {
                        FileUtils.deleteDirectory(this.serviceDirectory);
                    } catch (IOException e1) {
                        CloudAPI.getInstance().getConsole().error("Temp service directory of " + this.service.getServiceName() + " cannot be deleted (" + this.serviceDirectory.getAbsolutePath() + ")", e1);
                    }
                }

            }
        }, "redicloud-service-" + this.service.getServiceName());
        this.thread.start();

        return true;
    }

    private void destroyScreen() {
        if (this.screen == null) return;
        ScreenDestroyPacket screenDestroyPacket = null;
        for (UUID nodeId : this.service.getConsoleNodeListenerIds()) {
            if (nodeId.equals(NodeLauncher.getInstance().getNode().getUniqueId())) continue;
            ICloudNode node = CloudAPI.getInstance().getNodeManager().getNode(nodeId);
            if (screenDestroyPacket == null) {
                screenDestroyPacket = new ScreenDestroyPacket();
                screenDestroyPacket.getPacketData().setChannel(PacketChannel.NODE);
                screenDestroyPacket.setServiceId(this.service.getUniqueId());
            }
            screenDestroyPacket.getPacketData().addReceiver(node.getNetworkComponentInfo());
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
        if (this.service.isStatic() || !this.serviceDirectory.exists()) return;
        FileUtils.deleteDirectory(this.serviceDirectory);
    }

    public FutureAction<Boolean> deleteTempFilesAsync(boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if (isActive()) {
            stopProcess(force);
        }

        if (this.service.isStatic() || !this.serviceDirectory.exists()) {
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
            this.process.destroy();
        } else {
            CloudServiceInitStopPacket packet = new CloudServiceInitStopPacket();
            packet.getPacketData().addReceiver(this.service.getNetworkComponentInfo());
            packet.publishAsync();
            CloudAPI.getInstance().getScheduler().runTaskLaterAsync(() -> { // service crashed, force stop
                if(this.service.getServiceState() == ServiceState.RUNNING_DEFINED
                        || this.service.getServiceState() == ServiceState.RUNNING_UNDEFINED) {
                    this.process.destroy();
                }
            }, 1500, TimeUnit.MILLISECONDS);
        }
    }

    private List<String> getStartCommand(ICloudServiceVersion serviceVersion) {
        List<String> command = new ArrayList<>();

        command.add(serviceVersion.getJavaCommand());

        command.addAll(this.service.getConfiguration().getJvmArguments());


        command.add("-Xms" + this.service.getConfiguration().getMaxMemory() + "M");
        command.add("-Xmx" + this.service.getConfiguration().getMaxMemory() + "M");


        if (this.service.getEnvironment() == ServiceEnvironment.MINECRAFT) {
            command.add("-Dcom.mojang.eula.agree=true");
            command.add("-Djline.terminal=jline.UnsupportedTerminal");
        }

        command.add("-jar");
        command.add(this.serviceDirectory.getAbsolutePath() + File.separator + "service.jar");

        if (this.service.getEnvironment() == ServiceEnvironment.MINECRAFT) {
            command.add("nogui");
        }

        command.addAll(this.service.getConfiguration().getProcessParameters());

        return command;
    }
}
