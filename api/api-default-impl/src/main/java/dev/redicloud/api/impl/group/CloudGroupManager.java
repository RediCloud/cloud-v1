package dev.redicloud.api.impl.group;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.group.ICloudGroup;
import dev.redicloud.api.group.ICloudGroupManager;
import dev.redicloud.api.impl.redis.bucket.fetch.RedissonBucketFetchManager;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.template.ICloudServiceTemplate;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.commons.function.future.FutureActionCollection;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CloudGroupManager extends RedissonBucketFetchManager<CloudGroup, ICloudGroup> implements ICloudGroupManager {

    public CloudGroupManager() {
        super("serviceGroup", CloudGroup.class, "service_group_names");
    }

    @Override
    public FutureAction<ICloudGroup> getGroupAsync(UUID uniqueId) {
        return this.getBucketAsync(uniqueId.toString());
    }

    @Override
    public ICloudGroup getGroup(UUID uniqueId) {
        return this.getBucket(uniqueId.toString());
    }

    @Override
    public FutureAction<ICloudGroup> getGroupAsync(String name) {
        FutureAction<ICloudGroup> futureAction = new FutureAction<>();

        containsKeyInFetcherAsync(name.toLowerCase())
            .onFailure(futureAction)
            .onSuccess(contains -> {
                if (contains) {
                    getFetcherValueAsync(name.toLowerCase())
                        .onFailure(futureAction)
                        .onSuccess(uuid -> getBucketAsync(uuid)
                            .onFailure(futureAction)
                            .onSuccess(futureAction::complete));
                } else {
                    futureAction.completeExceptionally(new NullPointerException("Group not found"));
                }
            });

        return futureAction;
    }

    @Override
    public ICloudGroup getGroup(String name) {
        return getGroup(UUID.fromString(getFetcherValue(name.toLowerCase())));
    }

    @Override
    public FutureAction<Boolean> existsGroupAsync(UUID uniqueId) {
        return this.existsBucketAsync(uniqueId.toString());
    }

    @Override
    public boolean existsGroup(UUID uniqueId) {
        return this.existsBucket(uniqueId.toString());
    }

    @Override
    public FutureAction<Boolean> existsGroupAsync(String name) {
        return this.containsKeyInFetcherAsync(name.toLowerCase());
    }

    @Override
    public boolean existsGroup(String name) {
        return this.containsKeyInFetcher(name.toLowerCase());
    }

    @Override
    public Collection<ICloudGroup> getGroups() {
        return this.getBucketHolders();
    }

    @Override
    public FutureAction<Collection<ICloudGroup>> getGroupsAsync() {
        return this.getBucketHoldersAsync();
    }

    @Override
    public FutureAction<ICloudGroup> createGroupAsync(ICloudGroup group) {
        return this.createBucketAsync(group.getUniqueId().toString(), group);
    }

    @Override
    public FutureAction<Boolean> deleteGroupAsync(UUID uniqueId) {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        getGroupAsync(uniqueId)
            .onFailure(futureAction)
            .onSuccess(group -> {
                group.getConnectedServices()
                    .onFailure(futureAction)
                    .onSuccess(services -> {
                        FutureActionCollection<UUID, Boolean> futureActionFutureAction = new FutureActionCollection<>();
                        for (ICloudService service : services) {
                            futureActionFutureAction.addToProcess(service.getUniqueId(), CloudAPI.getInstance().getServiceManager().stopServiceAsync(service.getUniqueId(), true));
                        }
                        futureActionFutureAction.process()
                            .onFailure(futureAction)
                            .onSuccess(s1 -> {
                                this.deleteBucketAsync(group)
                                .onFailure(futureAction)
                                .onSuccess(s2 -> futureAction.complete(true));
                            });
                    });
            });

        return futureAction;
    }

    @Override
    public boolean deleteGroup(UUID uniqueId) throws Exception {
        ICloudGroup group = getGroup(uniqueId);
        for (ICloudService service : CloudAPI.getInstance().getServiceManager().getServices()) {
            if (service.getGroup() == null) continue;
            if (service.getGroupName().equalsIgnoreCase(group.getName())) {
                CloudAPI.getInstance().getServiceManager().stopServiceAsync(service.getUniqueId(), true).get(5, TimeUnit.SECONDS);
            }
        }
        this.deleteBucket(group);
        return true;
    }

    @Override
    public ICloudGroup createGroup(ICloudGroup group) {
        return this.createBucket(group.getUniqueId().toString(), group);
    }

    @Override
    public FutureAction<ICloudGroup> addDefaultTemplates(ICloudGroup group) {
        FutureAction<ICloudGroup> futureAction = new FutureAction<>();

        FutureActionCollection<String, Boolean> existencesCollection = new FutureActionCollection<>();

        existencesCollection.addToProcess("global-all",
                CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync("global-all"));
        switch (group.getServiceEnvironment()){
            case VELOCITY: {
                existencesCollection.addToProcess("global-velocity",
                        CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync("global-proxy"));
                break;
            }
            case BUNGEECORD: {
                existencesCollection.addToProcess("global-proxy",
                        CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync("global-proxy"));
                break;
            }
            case MINECRAFT:
                existencesCollection.addToProcess("global-minecraft",
                        CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync("global-minecraft"));
                break;
            case LIMBO:
                existencesCollection.addToProcess("global-limbo",
                        CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync("global-limbo"));
                break;
        }

        existencesCollection.process()
            .onFailure(futureAction)
            .onSuccess(existencesResults -> {
               FutureActionCollection<String, ICloudServiceTemplate> templateCollection = new FutureActionCollection<>();
               existencesResults.forEach((name, exists) -> {
                   if(!exists) return;
                   templateCollection.addToProcess(name, CloudAPI.getInstance().getServiceTemplateManager().getTemplateAsync(name));
               });
               templateCollection.process()
                   .onFailure(futureAction)
                   .onSuccess(templateResults -> {
                       for (ICloudServiceTemplate value : templateResults.values()) {
                           group.addTemplate(value);
                       }
                       group.updateAsync()
                           .onFailure(futureAction)
                           .onSuccess(s -> futureAction.complete(group));
                   });
            });

        return futureAction;
    }
}
