package net.suqatri.redicloud.api.node.poll.timeout;

import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.HashMap;
import java.util.UUID;

public interface ITimeOutPoll extends IRBucketObject {

    UUID getPollId();

    UUID getTimeOutTargetId();

    UUID getOpenerId();

    HashMap<UUID, TimeOutResult> getResults();

    FutureAction<TimeOutResult> decide();

    void manageResult(TimeOutResult result, UUID nodeId);

    void close();

    boolean isOpenerId();

}
