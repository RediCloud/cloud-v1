package net.suqatri.redicloud.api.impl.poll.timeout;

import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public interface ITimeOutPoll extends Serializable {

    UUID getPollId();
    UUID getTimeOutTargetId();
    UUID getOpenerId();

    HashMap<UUID, TimeOutResult> getResults();

    FutureAction<TimeOutResult> decide();

    void manageResult(TimeOutResult result, UUID nodeId);

    void close();

    boolean isOpenerId();

}
