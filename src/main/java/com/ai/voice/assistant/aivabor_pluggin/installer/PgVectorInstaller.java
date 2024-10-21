package com.ai.voice.assistant.aivabor_pluggin.installer;

import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;

/**
 * Strategy interface for pgvector installation.
 */
public interface PgVectorInstaller {
    void installPgVector(ProcessRequest request) throws Exception;
}
