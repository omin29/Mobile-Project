package com.example.taskmanager.task;

/**
 * Enumeration representing the possible states/statuses of a task.
 */
public enum TaskStatus {
    TODO(0), Finished(1), Failed(2);

    private final int value;
    TaskStatus(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of the status.
     * @return The status value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns a task status based on the provided status value.
     * @param value The integer value of a task status
     * @return Task status which matches the provided value
     */
    public static TaskStatus getStatus(int value) {
        for(TaskStatus status : TaskStatus.values()){
            if(status.getValue() == value) {
                return status;
            }
        }

        return null;
    }
}
