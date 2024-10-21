package com.ai.voice.assistant.aivabor_pluggin.installer;

import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Concrete strategy for installing pgvector in Sandbox Windows environment.
 */
@Component
@Slf4j
public class SandboxWindowsPgVectorInstaller implements PgVectorInstaller {

    @Override
    public void installPgVector(ProcessRequest request) throws Exception {
        log.info("Attempting to install pgvector on Windows...");

        // Installation on Windows requires manual steps or custom scripts.
        // Here's a placeholder to indicate that the implementation is pending.

        throw new UnsupportedOperationException("Installation of pgvector on Windows is not implemented.");
    }
}
