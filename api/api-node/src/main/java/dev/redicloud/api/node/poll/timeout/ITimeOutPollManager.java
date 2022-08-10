package dev.redicloud.api.node.poll.timeout;

import dev.redicloud.api.impl.configuration.impl.TimeOutPoolConfiguration;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ITimeOutPollManager {

    FutureAction<ITimeOutPoll> createPoll(ITimeOutPoll timeOutPool);

    FutureAction<ITimeOutPoll> getPoll(UUID pollId);

    FutureAction<Boolean> closePoll(ITimeOutPoll poolHolder);

    TimeOutPoolConfiguration getConfiguration();

}
