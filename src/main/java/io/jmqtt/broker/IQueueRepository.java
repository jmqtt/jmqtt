package io.jmqtt.broker;

import java.util.Queue;

public interface IQueueRepository {

    Queue<SessionRegistry.EnqueuedMessage> createQueue(String cli, boolean clean);
}
