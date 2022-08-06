package net.suqatri.redicloud.api.impl.poll.timeout;

import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ITimeOutPollManager {

    FutureAction<ITimeOutPoll> createPoll(ITimeOutPoll timeOutPool);

    FutureAction<ITimeOutPoll> getPoll(UUID pollId);

    FutureAction<Boolean> closePoll(ITimeOutPoll poolHolder);

}
