# PostgreSQL Counter Implementation

This project demonstrates the usage of different locking mechanisms with PostgreSQL to increment a counter in a concurrent environment.

## Implementation:

### Language:
- Java

### Dependencies:
- PostgreSQL JDBC Driver

### Counter Methods:
1. **Lost-Update** - Increments the counter without any locks, which might lead to lost updates in a concurrent scenario.
2. **In-place Update** - Directly increments the counter in the database without fetching its value first.
3. **Row-level Locking** - Uses PostgreSQL's `FOR UPDATE` clause to lock the row and prevent other threads from updating it concurrently.
4. **Optimistic Concurrency Control** - Uses a version column to ensure the row hasn't changed between the time it was read and the time an update is attempted.

## Execution Results:

- **Lost-Update**:
    - Time taken: 42200 ms
    - Counter value: 11581
    - Version: 0
- **In-place Update**:
    - Time taken: 37821 ms
    - Counter value: 100000
    - Version: 0
- **Row-level Locking**:
    - Time taken: 61814 ms
    - Counter value: 100000
    - Version: 0
- **Optimistic Concurrency Control**:
    - Time taken: 258211 ms
    - Counter value: 100000
    - Version: 100000

