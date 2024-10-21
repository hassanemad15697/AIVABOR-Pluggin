package com.ai.voice.assistant.aivabor_pluggin.service;

import com.ai.voice.assistant.aivabor_pluggin.installer.DockerPgVectorInstaller;
import com.ai.voice.assistant.aivabor_pluggin.installer.PgVectorInstaller;
import com.ai.voice.assistant.aivabor_pluggin.installer.SandboxLinuxPgVectorInstaller;
import com.ai.voice.assistant.aivabor_pluggin.installer.SandboxWindowsPgVectorInstaller;
import com.ai.voice.assistant.aivabor_pluggin.model.DatabaseDialect;
import com.ai.voice.assistant.aivabor_pluggin.model.EnvironmentType;
import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;

/**
 * Service for managing the pgvector extension.
 */
@Service
@Slf4j
public class PgVectorService {

    private final DataSourceService dataSourceService;

    // Strategy pattern map
    private final Map<EnvironmentType, PgVectorInstaller> installerMap = new EnumMap<>(EnvironmentType.class);

    /**
     * Registers installers for different environments.
     */
    public PgVectorService(DataSourceService dataSourceService,
                           DockerPgVectorInstaller dockerInstaller,
                           SandboxLinuxPgVectorInstaller sandboxLinuxInstaller,
                           SandboxWindowsPgVectorInstaller sandboxWindowsInstaller) {
        this.dataSourceService = dataSourceService;
        installerMap.put(EnvironmentType.DOCKER, dockerInstaller);
        installerMap.put(EnvironmentType.SANDBOX_LINUX, sandboxLinuxInstaller);
        installerMap.put(EnvironmentType.SANDBOX_WINDOWS, sandboxWindowsInstaller);
    }

    /**
     * Checks if pgvector is installed in the database.
     */
    public boolean isPgVectorInstalled() throws SQLException {
        try (Connection conn = dataSourceService.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            String checkExtensionSql = "SELECT COUNT(*) FROM pg_extension WHERE extname = 'vector'";
            try (ResultSet rs = stmt.executeQuery(checkExtensionSql)) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Installs pgvector if needed based on the environment.
     */
    public void installPgVectorIfNeeded(ProcessRequest request, EnvironmentType environmentType) throws Exception {
        if (dataSourceService.getDialect() == DatabaseDialect.POSTGRESQL) {
            if (!isPgVectorInstalled()) {
                PgVectorInstaller installer = installerMap.get(environmentType);
                if (installer == null) {
                    throw new Exception("No installer found for environment: " + environmentType);
                }
                installer.installPgVector(request);
                enablePgVectorExtensionInDatabase();
            } else {
                log.info("pgvector extension is already installed.");
            }
        }
    }

    /**
     * Enables the pgvector extension in the database.
     */
    private void enablePgVectorExtensionInDatabase() throws Exception {
        try (Connection conn = dataSourceService.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.info("Successfully enabled pgvector extension in the database.");
        } catch (SQLException e) {
            log.error("Failed to enable pgvector extension: {}", e.getMessage());
            throw new Exception("Failed to enable pgvector extension in the database.");
        }
    }
}
