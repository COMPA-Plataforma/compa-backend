package com.compa.dto.response;

import com.compa.enums.EmotionalState;
import com.compa.enums.TaskBarrier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CheckInDetailResponse {
    private Long id;
    private LocalDate checkInDate;
    private EmotionalState emotionalState;
    private String emotionalStateIcon;
    private String emotionalStateLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TaskDetailResponse> tasks;

    public static class TaskDetailResponse {
        private Long taskId;
        private String taskName;
        private String taskDescription;
        private boolean completed;
        private TaskBarrier barrier;
        private String barrierLabel;
        private String comment;

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final TaskDetailResponse obj = new TaskDetailResponse();
            public Builder taskId(Long v) { obj.taskId = v; return this; }
            public Builder taskName(String v) { obj.taskName = v; return this; }
            public Builder taskDescription(String v) { obj.taskDescription = v; return this; }
            public Builder completed(boolean v) { obj.completed = v; return this; }
            public Builder barrier(TaskBarrier v) { obj.barrier = v; return this; }
            public Builder barrierLabel(String v) { obj.barrierLabel = v; return this; }
            public Builder comment(String v) { obj.comment = v; return this; }
            public TaskDetailResponse build() { return obj; }
        }

        public Long getTaskId() { return taskId; }
        public String getTaskName() { return taskName; }
        public String getTaskDescription() { return taskDescription; }
        public boolean isCompleted() { return completed; }
        public TaskBarrier getBarrier() { return barrier; }
        public String getBarrierLabel() { return barrierLabel; }
        public String getComment() { return comment; }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final CheckInDetailResponse obj = new CheckInDetailResponse();
        public Builder id(Long v) { obj.id = v; return this; }
        public Builder checkInDate(LocalDate v) { obj.checkInDate = v; return this; }
        public Builder emotionalState(EmotionalState v) { obj.emotionalState = v; return this; }
        public Builder emotionalStateIcon(String v) { obj.emotionalStateIcon = v; return this; }
        public Builder emotionalStateLabel(String v) { obj.emotionalStateLabel = v; return this; }
        public Builder createdAt(LocalDateTime v) { obj.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { obj.updatedAt = v; return this; }
        public Builder tasks(List<TaskDetailResponse> v) { obj.tasks = v; return this; }
        public CheckInDetailResponse build() { return obj; }
    }

    public Long getId() { return id; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public EmotionalState getEmotionalState() { return emotionalState; }
    public String getEmotionalStateIcon() { return emotionalStateIcon; }
    public String getEmotionalStateLabel() { return emotionalStateLabel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<TaskDetailResponse> getTasks() { return tasks; }
}
    

