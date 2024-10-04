
# Hazelcast Counter Implementation and Report

This project demonstrates the usage of different locking mechanisms with Hazelcast to increment a counter in a concurrent environment.

## Implementation:

### Language:
- Java

### Dependencies:
- Hazelcast 5.3.2

### Counter Methods:
1. **Increment Without Locks** - This method directly increments the counter without any locks, which might lead to race conditions.
2. **Increment with Pessimistic Locking** - Uses Hazelcast's built-in locking mechanism to ensure that no two threads can increment the counter simultaneously.
3. **Increment with Optimistic Locking** - When updating the counter, it checks the version of the counter value and only updates if the version matches the expected version. If another thread has updated the counter in the meantime, causing a version mismatch, the operation is retried until it succeeds.
4. **Increment with IAtomicLong** - Uses Hazelcast's `IAtomicLong` for atomic increments.

## Execution Results:

- **Without Locks**:
    - Time taken: 8085 ms
    - Counter value: 34049
- **Pessimistic Locking**:
    - Time taken: 54863 ms
    - Counter value: 100000
- **Optimistic Locking**:
    - Time taken: 17385 ms
    - Counter value: 100000
- **IAtomicLong**:
    - Time taken: 15767 ms
    - Counter value: 100000

## Log Output:

Hazelcast nodes log output is provided in the `hazelcast-log.txt` file. This log includes details about the Hazelcast instance, cluster details, and other diagnostics.
