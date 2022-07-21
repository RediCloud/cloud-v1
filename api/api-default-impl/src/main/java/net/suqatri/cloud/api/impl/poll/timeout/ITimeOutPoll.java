package net.suqatri.cloud.api.impl.poll.timeout;

import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.Serializable;
import java.util.UUID;

public interface ITimeOutPoll extends Serializable {

    UUID getPollId();
    UUID getTimeOutTargetId();
    UUID getOpenerId();

    FutureAction<TimeOutResult> decide();
    void manageResult(TimeOutResult result, UUID nodeId);

    boolean isOpenerId(UUID uniqueId);

}
