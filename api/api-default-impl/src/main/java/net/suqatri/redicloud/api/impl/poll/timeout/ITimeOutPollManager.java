package net.suqatri.redicloud.api.impl.poll.timeout;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ITimeOutPollManager {

    FutureAction<IRBucketHolder<ITimeOutPoll>> createPoll(ITimeOutPoll timeOutPool);

    FutureAction<IRBucketHolder<ITimeOutPoll>> getPoll(UUID pollId);

    FutureAction<Boolean> closePoll(IRBucketHolder<ITimeOutPoll> poolHolder);

}
