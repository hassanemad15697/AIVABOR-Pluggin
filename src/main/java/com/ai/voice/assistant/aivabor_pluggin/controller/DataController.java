package com.ai.voice.assistant.aivabor_pluggin.controller;

import com.ai.voice.assistant.aivabor_pluggin.model.EnvironmentType;
import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;
import com.ai.voice.assistant.aivabor_pluggin.service.DataProcessingFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for data processing endpoints.
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final DataProcessingFacadeService dataProcessingFacadeService;

    /**
     * Endpoint to process data when the database is running in Docker.
     */
    @PostMapping("/process/docker")
    public ResponseEntity<String> processDataDocker(@RequestBody ProcessRequest request) {
        return processData(request, EnvironmentType.DOCKER);
    }

    /**
     * Endpoint to process data when the database is running on a sandbox Linux.
     */
    @PostMapping("/process/sandbox/linux")
    public ResponseEntity<String> processDataSandboxLinux(@RequestBody ProcessRequest request) {
        return processData(request, EnvironmentType.SANDBOX_LINUX);
    }

    /**
     * Endpoint to process data when the database is running on a sandbox Windows.
     */
    @PostMapping("/process/sandbox/windows")
    public ResponseEntity<String> processDataSandboxWindows(@RequestBody ProcessRequest request) {
        return processData(request, EnvironmentType.SANDBOX_WINDOWS);
    }

    /**
     * Generic method to process data based on environment type.
     */
    private ResponseEntity<String> processData(ProcessRequest request, EnvironmentType environmentType) {
        try {
            dataProcessingFacadeService.processData(request, environmentType);
            return ResponseEntity.ok("Data processing and insertion completed.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process data: " + e.getMessage());
        }
    }
}
