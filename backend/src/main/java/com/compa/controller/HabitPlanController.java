package com.compa.controller;

import com.compa.dto.request.HabitPlanRequest;
import com.compa.dto.request.HabitTaskRequest;
import com.compa.model.HabitPlan;
import com.compa.model.HabitTask;
import com.compa.service.HabitPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;

@RestController
@RequestMapping("/api/estudiantes/{estudianteId}/habit-plans")
@CrossOrigin(origins = "*")
public class HabitPlanController {

    private final HabitPlanService habitPlanService;

    public HabitPlanController(HabitPlanService habitPlanService) {
        this.habitPlanService = habitPlanService;
    }

    @PostMapping
    public ResponseEntity<HabitPlan> createPlan(@PathVariable Long estudianteId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody HabitPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(habitPlanService.createPlan(estudianteId, userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<HabitPlan>> getPlansByEstudiante(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(habitPlanService.getPlansByEstudiante(estudianteId));
    }

    @GetMapping("/active")
    public ResponseEntity<HabitPlan> getActivePlan(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(habitPlanService.getActivePlan(estudianteId));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<HabitPlan> getPlanById(@PathVariable Long estudianteId, @PathVariable Long planId) {
        return ResponseEntity.ok(habitPlanService.getPlanById(planId));
    }

    @PutMapping("/{planId}")
    public ResponseEntity<HabitPlan> updatePlan(@PathVariable Long estudianteId, @PathVariable Long planId,
            @Valid @RequestBody HabitPlanRequest request) {
        return ResponseEntity.ok(habitPlanService.updatePlan(planId, request));
    }

    @PatchMapping("/{planId}/deactivate")
    public ResponseEntity<HabitPlan> deactivatePlan(@PathVariable Long estudianteId, @PathVariable Long planId) {
        return ResponseEntity.ok(habitPlanService.deactivatePlan(planId));
    }

    @PostMapping("/{planId}/tasks")
    public ResponseEntity<HabitTask> addTask(@PathVariable Long estudianteId, @PathVariable Long planId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody HabitTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(habitPlanService.addTask(estudianteId, planId, userDetails.getUsername(), request));
    }

    @DeleteMapping("/{planId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long estudianteId, @PathVariable Long planId,
            @PathVariable Long taskId) {
        habitPlanService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/today")
    public ResponseEntity<List<HabitTask>> getTasksForToday(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(habitPlanService.getTasksForToday(estudianteId));
    }
}