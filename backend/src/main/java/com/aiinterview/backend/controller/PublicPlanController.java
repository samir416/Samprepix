package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.admin.PlanResponse;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PublicPlanController {

    private final PlanRepository planRepository;

    @GetMapping({"", "/", "/active", "/active/"})
    public ResponseEntity<List<PlanResponse>> getActivePlans() {
        List<Plan> plans = planRepository.findByActiveTrue();
        return ResponseEntity.ok(
                plans.stream()
                        .map(PlanResponse::fromEntity)
                        .toList()
        );
    }
}
