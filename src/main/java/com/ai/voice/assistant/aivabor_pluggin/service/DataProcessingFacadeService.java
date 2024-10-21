package com.ai.voice.assistant.aivabor_pluggin.service;

import com.ai.voice.assistant.aivabor_pluggin.model.EnvironmentType;
import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Facade service for data processing.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataProcessingFacadeService {

    private final DataSourceService dataSourceService;
    private final PgVectorService pgVectorService;
    private final DataProcessingService dataProcessingService;

    /**
     * Processes data based on the environment type.
     */
    public void processData(ProcessRequest request, EnvironmentType environmentType) throws Exception {
        dataProcessingService.validateIdentifiers(request);
        dataSourceService.initializeDataSource(request);
        pgVectorService.installPgVectorIfNeeded(request, environmentType);

        dataProcessingService.createVectorTable(request);
        dataProcessingService.processAndInsertData(request);
    }
}
