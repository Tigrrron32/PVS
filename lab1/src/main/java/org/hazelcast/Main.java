package org.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;

public class Main {

    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type", "slf4j");

        Config config = new Config();
        config.getCPSubsystemConfig().setCPMemberCount(3);
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance instance3 = Hazelcast.newHazelcastInstance(config);

        HazelcastCounter counter = new HazelcastCounter(instance1);

        System.out.println("Incrementing without locks:");
        counter.runThreadsAndMeasure(counter::incrementWithoutLocks);
        System.out.println("Counter value: " + counter.counterMap.get("counter"));

        System.out.println("Incrementing with pessimistic locking:");
        counter.runThreadsAndMeasure(counter::incrementWithPessimLocking);
        System.out.println("Counter value: " + counter.counterMap.get("counter"));

        System.out.println("Incrementing with optimistic locking:");
        counter.runThreadsAndMeasure(counter::incrementWithOptimLocking);
        System.out.println("Counter value: " + counter.counterMap.get("counter"));

        System.out.println("Incrementing with IAtomicLong:");
        counter.runThreadsAndMeasure(counter::incrementWithIAtomicLong);
        IAtomicLong atomicLong = counter.hazelcastInstance.getCPSubsystem().getAtomicLong("atomicCounter");
        System.out.println("Counter value: " + atomicLong.get());

        System.exit(0);
    }
}