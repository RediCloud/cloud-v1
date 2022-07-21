package net.suqatri.cloud.api.impl.poll.timeout;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ITimeOutPollManager {

    FutureAction<IRBucketHolder<ITimeOutPoll>> createPoll(ITimeOutPoll timeOutPool);
    FutureAction<IRBucketHolder<ITimeOutPoll>> getPoll(UUID pollId);
    FutureAction<Boolean> closePoll(IRBucketHolder<ITimeOutPoll> poolHolder);

}
