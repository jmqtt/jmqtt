/*
 * Copyright (c) 2012-2018 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.jmqtt.broker.subscriptions;


import io.netty.handler.codec.mqtt.MqttQoS;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CTrieTest {

    private CTrie sut;

    @Before
    public void setUp() {
        sut = new CTrie();
    }

    @Test
    public void testAddOnSecondLayerWithEmptyTokenOnEmptyTree() {
        //Exercise
        sut.addToTree(clientSubOnTopic("TempSensor1", "/"));

        //Verify
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/"));
        assertTrue("Node on path / must be present", matchedNode.isPresent());
        //verify structure, only root INode and the first CNode should be present
        assertThat(this.sut.root.mainNode().subscriptions).isEmpty();
        assertThat(this.sut.root.mainNode().allChildren()).isNotEmpty();

        INode firstLayer = this.sut.root.mainNode().allChildren().get(0);
        assertThat(firstLayer.mainNode().subscriptions).isEmpty();
        assertThat(firstLayer.mainNode().allChildren()).isNotEmpty();

        INode secondLayer = firstLayer.mainNode().allChildren().get(0);
        assertThat(secondLayer.mainNode().subscriptions).isNotEmpty();
        assertThat(secondLayer.mainNode().allChildren()).isEmpty();
    }

    @Test
    public void testAddFirstLayerNodeOnEmptyTree() {
        //Exercise
        sut.addToTree(clientSubOnTopic("TempSensor1", "/temp"));

        //Verify
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/temp"));
        assertTrue("Node on path /temp must be present", matchedNode.isPresent());
        assertFalse(matchedNode.get().subscriptions.isEmpty());
    }

    @Test
    public void testLookup() {
        final Subscription existingSubscription = clientSubOnTopic("TempSensor1", "/temp");
        sut.addToTree(existingSubscription);

        //Exercise
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/humidity"));

        //Verify
        assertFalse("Node on path /humidity can't be present", matchedNode.isPresent());
    }

    @Test
    public void testAddNewSubscriptionOnExistingNode() {
        final Subscription existingSubscription = clientSubOnTopic("TempSensor1", "/temp");
        sut.addToTree(existingSubscription);

        //Exercise
        final Subscription newSubscription = clientSubOnTopic("TempSensor2", "/temp");
        sut.addToTree(newSubscription);

        //Verify
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/temp"));
        assertTrue("Node on path /temp must be present", matchedNode.isPresent());
        final Set<Subscription> subscriptions = matchedNode.get().subscriptions;
        assertTrue(subscriptions.contains(newSubscription));
    }

    @Test
    public void testAddNewDeepNodes() {
        sut.addToTree(clientSubOnTopic("TempSensorRM", "/italy/roma/temp"));
        sut.addToTree(clientSubOnTopic("TempSensorFI", "/italy/firenze/temp"));
        sut.addToTree(clientSubOnTopic("HumSensorFI", "/italy/roma/humidity"));
        final Subscription happinessSensor = clientSubOnTopic("HappinessSensor", "/italy/happiness");
        sut.addToTree(happinessSensor);

        //Verify
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/italy/happiness"));
        assertTrue("Node on path /italy/happiness must be present", matchedNode.isPresent());
        final Set<Subscription> subscriptions = matchedNode.get().subscriptions;
        assertTrue(subscriptions.contains(happinessSensor));
    }

    static Subscription clientSubOnTopic(String clientID, String topicName) {
        return new Subscription(clientID, Topic.asTopic(topicName), null);
    }

    @Test
    public void givenTreeWithSomeNodeWhenRemoveContainedSubscriptionThenNodeIsUpdated() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "/temp"));

        //Exercise
        sut.removeFromTree(Topic.asTopic("/temp"), "TempSensor1");

        //Verify
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/temp"));
        assertFalse("Node on path /temp can't be present", matchedNode.isPresent());
    }

    @Test
    public void givenTreeWithSomeNodeUnsubscribeAndResubscribeCleanTomb() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "test"));
        sut.removeFromTree(Topic.asTopic("test"), "TempSensor1");

        sut.addToTree(clientSubOnTopic("TempSensor1", "test"));
        assertTrue(sut.root.mainNode().allChildren().size() == 1);  // looking to see if TNode is cleaned up
    }

    @Test
    public void givenTreeWithSomeNodeWhenRemoveMultipleTimes() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "test"));

        // make sure no TNode exceptions
        sut.removeFromTree(Topic.asTopic("test"), "TempSensor1");
        sut.removeFromTree(Topic.asTopic("test"), "TempSensor1");
        sut.removeFromTree(Topic.asTopic("test"), "TempSensor1");
        sut.removeFromTree(Topic.asTopic("test"), "TempSensor1");

        //Verify
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/temp"));
        assertFalse("Node on path /temp can't be present", matchedNode.isPresent());
    }

    @Test
    public void givenTreeWithSomeDeepNodeWhenRemoveMultipleTimes() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "/test/me/1/2/3"));

        // make sure no TNode exceptions
        sut.removeFromTree(Topic.asTopic("/test/me/1/2/3"), "TempSensor1");
        sut.removeFromTree(Topic.asTopic("/test/me/1/2/3"), "TempSensor1");
        sut.removeFromTree(Topic.asTopic("/test/me/1/2/3"), "TempSensor1");

        //Verify
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/temp"));
        assertFalse("Node on path /temp can't be present", matchedNode.isPresent());
    }

    @Test
    public void givenTreeWithSomeNodeHierarchWhenRemoveContainedSubscriptionThenNodeIsUpdated() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "/temp/1"));
        sut.addToTree(clientSubOnTopic("TempSensor1", "/temp/2"));

        //Exercise
        sut.removeFromTree(Topic.asTopic("/temp/1"), "TempSensor1");

        sut.removeFromTree(Topic.asTopic("/temp/1"), "TempSensor1");
        final Set<Subscription> matchingSubs = sut.recursiveMatch(Topic.asTopic("/temp/2"));

        //Verify
        final Subscription expectedMatchingsub = new Subscription("TempSensor1", Topic.asTopic("/temp/2"), MqttQoS.AT_MOST_ONCE);
        assertThat(matchingSubs).contains(expectedMatchingsub);
    }

    @Test
    public void givenTreeWithSomeNodeHierarchWhenRemoveContainedSubscriptionSmallerThenNodeIsNotUpdated() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "/temp/1"));
        sut.addToTree(clientSubOnTopic("TempSensor1", "/temp/2"));

        //Exercise
        sut.removeFromTree(Topic.asTopic("/temp"), "TempSensor1");

        final Set<Subscription> matchingSubs1 = sut.recursiveMatch(Topic.asTopic("/temp/1"));
        final Set<Subscription> matchingSubs2 = sut.recursiveMatch(Topic.asTopic("/temp/2"));

        //Verify
        // not clear to me, but I believe /temp unsubscribe should not unsub you from downstream /temp/1 or /temp/2
        final Subscription expectedMatchingsub1 = new Subscription("TempSensor1", Topic.asTopic("/temp/1"), MqttQoS.AT_MOST_ONCE);
        assertThat(matchingSubs1).contains(expectedMatchingsub1);
        final Subscription expectedMatchingsub2 = new Subscription("TempSensor1", Topic.asTopic("/temp/2"), MqttQoS.AT_MOST_ONCE);
        assertThat(matchingSubs2).contains(expectedMatchingsub2);
    }

    @Test
    public void givenTreeWithDeepNodeWhenRemoveContainedSubscriptionThenNodeIsUpdated() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "/bah/bin/bash"));

        sut.removeFromTree(Topic.asTopic("/bah/bin/bash"), "TempSensor1");

        //Verify
        final Optional<CNode> matchedNode = sut.lookup(Topic.asTopic("/bah/bin/bash"));
        assertFalse("Node on path /temp can't be present", matchedNode.isPresent());
    }

    @Test
    public void testMatchSubscriptionNoWildcards() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "/temp"));

        //Exercise
        final Set<Subscription> matchingSubs = sut.recursiveMatch(Topic.asTopic("/temp"));

        //Verify
        final Subscription expectedMatchingsub = new Subscription("TempSensor1", Topic.asTopic("/temp"), MqttQoS.AT_MOST_ONCE);
        assertThat(matchingSubs).contains(expectedMatchingsub);
    }

    @Test
    public void testRemovalInnerTopicOffRootSameClient() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "temp"));
        sut.addToTree(clientSubOnTopic("TempSensor1", "temp/1"));

        //Exercise
        final Set<Subscription> matchingSubs1 = sut.recursiveMatch(Topic.asTopic("temp"));
        final Set<Subscription> matchingSubs2 = sut.recursiveMatch(Topic.asTopic("temp/1"));

        //Verify
        final Subscription expectedMatchingsub1 = new Subscription("TempSensor1", Topic.asTopic("temp"), MqttQoS.AT_MOST_ONCE);
        final Subscription expectedMatchingsub2 = new Subscription("TempSensor1", Topic.asTopic("temp/1"), MqttQoS.AT_MOST_ONCE);

        assertThat(matchingSubs1).contains(expectedMatchingsub1);
        assertThat(matchingSubs2).contains(expectedMatchingsub2);

        sut.removeFromTree(Topic.asTopic("temp"), "TempSensor1");

        //Exercise
        final Set<Subscription> matchingSubs3 = sut.recursiveMatch(Topic.asTopic("temp"));
        final Set<Subscription> matchingSubs4 = sut.recursiveMatch(Topic.asTopic("temp/1"));

        assertThat(matchingSubs3).doesNotContain(expectedMatchingsub1);
        assertThat(matchingSubs4).contains(expectedMatchingsub2);
    }

    @Test
    public void testRemovalInnerTopicOffRootDiffClient() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "temp"));
        sut.addToTree(clientSubOnTopic("TempSensor2", "temp/1"));

        //Exercise
        final Set<Subscription> matchingSubs1 = sut.recursiveMatch(Topic.asTopic("temp"));
        final Set<Subscription> matchingSubs2 = sut.recursiveMatch(Topic.asTopic("temp/1"));

        //Verify
        final Subscription expectedMatchingsub1 = new Subscription("TempSensor1", Topic.asTopic("temp"), MqttQoS.AT_MOST_ONCE);
        final Subscription expectedMatchingsub2 = new Subscription("TempSensor2", Topic.asTopic("temp/1"), MqttQoS.AT_MOST_ONCE);

        assertThat(matchingSubs1).contains(expectedMatchingsub1);
        assertThat(matchingSubs2).contains(expectedMatchingsub2);

        sut.removeFromTree(Topic.asTopic("temp"), "TempSensor1");

        //Exercise
        final Set<Subscription> matchingSubs3 = sut.recursiveMatch(Topic.asTopic("temp"));
        final Set<Subscription> matchingSubs4 = sut.recursiveMatch(Topic.asTopic("temp/1"));

        assertThat(matchingSubs3).doesNotContain(expectedMatchingsub1);
        assertThat(matchingSubs4).contains(expectedMatchingsub2);
    }

    @Test
    public void testRemovalOuterTopicOffRootDiffClient() {
        sut.addToTree(clientSubOnTopic("TempSensor1", "temp"));
        sut.addToTree(clientSubOnTopic("TempSensor2", "temp/1"));

        //Exercise
        final Set<Subscription> matchingSubs1 = sut.recursiveMatch(Topic.asTopic("temp"));
        final Set<Subscription> matchingSubs2 = sut.recursiveMatch(Topic.asTopic("temp/1"));

        //Verify
        final Subscription expectedMatchingsub1 = new Subscription("TempSensor1", Topic.asTopic("temp"), MqttQoS.AT_MOST_ONCE);
        final Subscription expectedMatchingsub2 = new Subscription("TempSensor2", Topic.asTopic("temp/1"), MqttQoS.AT_MOST_ONCE);

        assertThat(matchingSubs1).contains(expectedMatchingsub1);
        assertThat(matchingSubs2).contains(expectedMatchingsub2);

        sut.removeFromTree(Topic.asTopic("temp/1"), "TempSensor2");

        //Exercise
        final Set<Subscription> matchingSubs3 = sut.recursiveMatch(Topic.asTopic("temp"));
        final Set<Subscription> matchingSubs4 = sut.recursiveMatch(Topic.asTopic("temp/1"));

        assertThat(matchingSubs3).contains(expectedMatchingsub1);
        assertThat(matchingSubs4).doesNotContain(expectedMatchingsub2);
    }
}
