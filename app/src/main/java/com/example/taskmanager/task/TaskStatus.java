package com.example.taskmanager.task;

public enum TaskStatus {
    TODO(0), Finished(1), Failed(2);

    private final int value;
    private TaskStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    public static TaskStatus getStatus(int value) {
        for(TaskStatus status : TaskStatus.values()){
            if(status.getValue() == value) {
                return status;
            }
        }

        return null;
    }
}
