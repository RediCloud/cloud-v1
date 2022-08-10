package dev.redicloud.api.group;

import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudGroupManager {

    FutureAction<ICloudGroup> getGroupAsync(UUID uniqueId);

    ICloudGroup getGroup(UUID uniqueId);

    FutureAction<ICloudGroup> getGroupAsync(String name);

    ICloudGroup getGroup(String name);

    FutureAction<Boolean> existsGroupAsync(UUID uniqueId);

    boolean existsGroup(UUID uniqueId);

    FutureAction<Boolean> existsGroupAsync(String name);

    boolean existsGroup(String name);

    Collection<ICloudGroup> getGroups();

    FutureAction<Collection<ICloudGroup>> getGroupsAsync();

    ICloudGroup createGroup(ICloudGroup group);

    FutureAction<ICloudGroup> createGroupAsync(ICloudGroup group);

    FutureAction<Boolean> deleteGroupAsync(UUID uniqueId);

    boolean deleteGroup(UUID uniqueId) throws Exception;

    FutureAction<ICloudGroup> addDefaultTemplates(ICloudGroup group);

}
