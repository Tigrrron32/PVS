package org.hazelcast;

import com.hazelcast.core.*;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.map.IMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HazelcastCounter {
    HazelcastInstance hazelcastInstance;
    IMap<String, VersionedValue> counterMap;

    public HazelcastCounter(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        counterMap = hazelcastInstance.getMap("counterMap");
    }

    public void resetCounter() {
        counterMap.put("counter", new VersionedValue(0, 0));
        IAtomicLong atomicLong = hazelcastInstance.getCPSubsystem().getAtomicLong("atomicCounter");
        atomicLong.set(0);
    }

    public void incrementWithoutLocks() {
        VersionedValue currentValue = counterMap.getOrDefault("counter", new VersionedValue(0, 0));
        counterMap.put("counter", new VersionedValue(currentValue.value + 1, currentValue.version + 1));
    }

    public void incrementWithPessimLocking() {
        counterMap.lock("counter");
        try {
            VersionedValue currentValue = counterMap.getOrDefault("counter", new VersionedValue(0, 0));
            counterMap.put("counter", new VersionedValue(currentValue.value + 1, currentValue.version + 1));
        } finally {
            counterMap.unlock("counter");
        }
    }

    public void incrementWithOptimLocking() {
        boolean updated = false;
        while (!updated) {
            VersionedValue originalValue = counterMap.get("counter");
            VersionedValue newValue = new VersionedValue(originalValue.value + 1, originalValue.version + 1);
            updated = counterMap.replace("counter", originalValue, newValue);
        }
    }

    public void incrementWithIAtomicLong() {
        IAtomicLong atomicLong = hazelcastInstance.getCPSubsystem().getAtomicLong("atomicCounter");
        atomicLong.incrementAndGet();
    }

    public void runThreadsAndMeasure(Runnable incrementMethod) {
        resetCounter(); // Спочатку скидаємо значення лічильника
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Створюємо пул з 10 потоків
        long startTime = System.currentTimeMillis(); // Початок вимірювання часу

        // Запускаємо 10 потоків
        for (int t = 0; t < 10; t++) {
            executorService.submit(() -> {
                // Кожен потік виконує 10 000 інкрементів
                for (int i = 0; i < 10000; i++) {
                    incrementMethod.run(); // Викликаємо метод інкременту
                }
            });
        }

        // Завершуємо виконання потоків
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES); // Чекаємо завершення всіх потоків
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis(); // Кінець вимірювання часу
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }

    public static class VersionedValue {
        public int value;
        public int version;

        public VersionedValue(int value, int version) {
            this.value = value;
            this.version = version;
        }

        @Override
        public String toString() {
            return "" + value;
        }
    }
}