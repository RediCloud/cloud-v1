package net.suqatri.redicloud.node.commands;

import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersion;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.service.version.ServiceVersionProperty;
import net.suqatri.redicloud.node.console.setup.SetupControlState;
import net.suqatri.redicloud.node.setup.version.ServiceVersionSetup;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("sv|serviceversion")
public class ServiceVersionCommand extends ConsoleCommand {

    /*
    /sv create <Name>
    /sv delete <Name>
    /sv list
    /sv edit <Name> <Property> <Value>
    /sv redownload <Name>
    /sv repatch <Name>
     */

    @HelpCommand
    @Default
    @Description("Show help for service commands")
    @Syntax("[Page]")
    public void onHelp(CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("create")
    @Description("Create a new service version")
    @Syntax("<Name>")
    public void onCreate(CommandSender commandSender, String name) {
        CloudAPI.getInstance().getServiceVersionManager().existsServiceVersionAsync(name)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to check if service version exists!", t))
                .onSuccess(exists -> {
                    if (exists) {
                        commandSender.sendMessage("Service version already exists!");
                        return;
                    }
                    new ServiceVersionSetup().start((setup, state) -> {
                        if (state == SetupControlState.CANCELLED) {
                            commandSender.sendMessage("Setup cancelled!");
                            return;
                        }
                        CloudServiceVersion serviceVersion = new CloudServiceVersion();
                        serviceVersion.setName(name);
                        serviceVersion.setJavaCommand(setup.getJavaCommand());
                        serviceVersion.setDownloadUrl(setup.getDownloadUrl());
                        serviceVersion.setEnvironmentType(setup.getEnvironment());
                        serviceVersion.setPaperClip(setup.isPaperClip());
                        CloudAPI.getInstance().getServiceVersionManager().createServiceVersionAsync(serviceVersion)
                                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to create service version!", t))
                                .onSuccess(versionHolder -> {
                                    commandSender.sendMessage("Service version created!");
                                });
                    });
                });
    }

    @Subcommand("delete")
    @Description("Delete a service version")
    @Syntax("<Name>")
    @CommandCompletion("@service_versions")
    public void onDelete(CommandSender commandSender, String name) {
        CloudAPI.getInstance().getGroupManager().getGroupsAsync()
                .onFailure(t -> commandSender.sendMessage("Failed to get groups"))
                .onSuccess(groupHolders -> {
                    List<IRBucketHolder<ICloudGroup>> list = new ArrayList<>();
                    for (IRBucketHolder<ICloudGroup> groupHolder : groupHolders) {
                        if (groupHolder.get().getServiceVersionName().equalsIgnoreCase(name)) {
                            list.add(groupHolder);
                        }
                    }
                    if (!list.isEmpty()) {
                        StringBuilder builder = new StringBuilder();
                        for (IRBucketHolder<ICloudGroup> groupIRBucketHolder : list) {
                            if (!builder.toString().isEmpty()) builder.append("§8, ");
                            builder.append("%hc");
                            builder.append(groupIRBucketHolder.get().getName());
                        }
                        commandSender.sendMessage("Cannot delete service version %hc" + name + "%tc because it is used by groups: " + builder);
                        return;
                    }
                    CloudAPI.getInstance().getServiceVersionManager().existsServiceVersionAsync(name)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while checking service version existence", t))
                            .onSuccess(exists -> {
                                if (!exists) {
                                    CloudAPI.getInstance().getConsole().error("Service version doesn't exist");
                                } else {
                                    CloudAPI.getInstance().getServiceVersionManager().deleteServiceVersionAsync(name)
                                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while deleting service version", t))
                                            .onSuccess(s -> CloudAPI.getInstance().getConsole().info("Service version deleted"));
                                }
                            });
                });
    }

    @Subcommand("list")
    @Description("List all service versions")
    public void onList(CommandSender commandSender) {
        CloudAPI.getInstance().getServiceVersionManager().getServiceVersionsAsync()
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while getting service versions", t))
                .onSuccess(versionHolders -> {
                    if (versionHolders.isEmpty()) {
                        CloudAPI.getInstance().getConsole().info("No service versions found");
                    } else {
                        CloudAPI.getInstance().getConsole().info("Service versions §8(%hc" + versionHolders.size() + "§8)%tc:");
                        for (IRBucketHolder<ICloudServiceVersion> versionHolder : versionHolders) {
                            CloudAPI.getInstance().getConsole().info("§8- %tc" + versionHolder.get().getName() + " §8| %hc" + versionHolder.get().getDownloadUrl());
                        }
                    }
                });
    }

    @Subcommand("edit")
    @Description("Edit a service version")
    @Syntax("<Name> <Property> <Value>")
    @CommandCompletion("@service_versions @service_version_properties")
    public void onEdit(CommandSender commandSender, String name, String propertyString, String value) {
        CloudAPI.getInstance().getServiceVersionManager().existsServiceVersionAsync(name)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while checking service version existence", t))
                .onSuccess(exists -> {
                    if (!exists) {
                        CloudAPI.getInstance().getConsole().error("Service version doesn't exist");
                        return;
                    }
                    CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(name)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while getting service version", t))
                            .onSuccess(versionHolder -> {
                                ServiceVersionProperty property = null;
                                switch (propertyString.toLowerCase()) {
                                    case "download_url":
                                    case "download":
                                    case "download-url":
                                    case "url":
                                        property = ServiceVersionProperty.DOWNLOAD_URL;
                                        break;
                                    case "paper_clip":
                                    case "paperclip":
                                    case "clip":
                                    case "paper-clip":
                                        property = ServiceVersionProperty.PAPER_CLIP;
                                        break;
                                }
                                if (property == null) {
                                    StringBuilder builder = new StringBuilder();
                                    for (ServiceVersionProperty p : ServiceVersionProperty.values()) {
                                        if (!builder.toString().isEmpty()) builder.append("§8, ");
                                        builder.append("%hc");
                                        builder.append("%hc");
                                        builder.append(p.name());
                                    }
                                    commandSender.sendMessage("Property must be one of: " + builder);
                                    return;
                                }
                                switch (property) {
                                    case DOWNLOAD_URL:
                                        versionHolder.get().setDownloadUrl(value);
                                        versionHolder.get().updateAsync()
                                                .onSuccess(v -> {
                                                    if (versionHolder.get().isPaperClip()) {
                                                        CloudAPI.getInstance().getServiceVersionManager().patchAsync(versionHolder, true);
                                                    }
                                                });
                                        break;
                                    case PAPER_CLIP:
                                        boolean paperClip = false;
                                        try {
                                            paperClip = Boolean.parseBoolean(value);
                                        } catch (Exception e) {
                                            commandSender.sendMessage("Invalid value! Must be a boolean");
                                            return;
                                        }
                                        versionHolder.get().setPaperClip(paperClip);
                                        versionHolder.get().updateAsync()
                                                .onSuccess(v -> {
                                                    if (versionHolder.get().isPaperClip()) {
                                                        CloudAPI.getInstance().getServiceVersionManager().patchAsync(versionHolder, true);
                                                    }
                                                });
                                        break;
                                }
                                commandSender.sendMessage("Property " + property.name().toLowerCase() + " was set to " + value);
                            });
                });
    }

    @Subcommand("patch")
    @Description("Patch a service version")
    @Syntax("<Name>")
    @CommandCompletion("@service_versions")
    public void onPatch(CommandSender commandSender, String serviceVersionName) {
        CloudAPI.getInstance().getServiceVersionManager().existsServiceVersionAsync(serviceVersionName)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while checking service version existence", t))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Service version doesn't exist");
                        return;
                    }
                    CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(serviceVersionName)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while getting service version", t))
                            .onSuccess(versionHolder -> {
                                if (!versionHolder.get().isPaperClip()) {
                                    CloudAPI.getInstance().getConsole().error("Service version is not paperclip");
                                    return;
                                }
                                CloudAPI.getInstance().getServiceVersionManager().patchAsync(versionHolder, true)
                                        .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while patching service version", t))
                                        .onSuccess(v -> commandSender.sendMessage("Service version patched"));
                            });
                });
    }

    @Subcommand("download")
    @Description("Download a service version")
    @Syntax("<Name>")
    @CommandCompletion("@service_versions")
    public void onDownload(CommandSender commandSender, String serviceVersionName) {
        CloudAPI.getInstance().getServiceVersionManager().existsServiceVersionAsync(serviceVersionName)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while checking service version existence", t))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Service version doesn't exist");
                        return;
                    }
                    CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(serviceVersionName)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while getting service version", t))
                            .onSuccess(versionHolder -> {
                                CloudAPI.getInstance().getServiceVersionManager().downloadAsync(versionHolder, true)
                                        .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while downloading service version", t))
                                        .onSuccess(v -> commandSender.sendMessage("Service version downloaded"));
                            });
                });
    }

}
