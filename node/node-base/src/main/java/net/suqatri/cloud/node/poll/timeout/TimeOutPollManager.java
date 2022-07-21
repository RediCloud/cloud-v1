package net.suqatri.cloud.node.poll.timeout;

import net.suqatri.cloud.api.impl.poll.timeout.ITimeOutPoll;
import net.suqatri.cloud.api.impl.poll.timeout.ITimeOutPollManager;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.UUID;

public class TimeOutPollManager extends RedissonBucketManager<TimeOutPoll, ITimeOutPoll> implements ITimeOutPollManager {

    public TimeOutPollManager() {
        super("timeouts", ITimeOutPoll.class);
    }

    @Override
    public FutureAction<IRBucketHolder<ITimeOutPoll>> createPoll(ITimeOutPoll timeOutPool) {
        return createBucketAsync(timeOutPool.getPollId().toString(), timeOutPool);
    }

    @Override
    public FutureAction<IRBucketHolder<ITimeOutPoll>> getPoll(UUID pollId) {
        return this.getBucketHolderAsync(pollId.toString());
    }

    @Override
    public FutureAction<Boolean> closePoll(IRBucketHolder<ITimeOutPoll> poolHolder) {
        return this.deleteBucketAsync(poolHolder.getIdentifier());
    }
}
