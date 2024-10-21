package com.ai.voice.assistant.aivabor_pluggin.installer;

import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;
import com.ai.voice.assistant.aivabor_pluggin.service.DockerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Concrete strategy for installing pgvector in Docker environment.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DockerPgVectorInstaller implements PgVectorInstaller {

    private final DockerService dockerService;

    @Override
    public void installPgVector(ProcessRequest request) throws Exception {
        String containerName = dockerService.getDockerContainerName(request.getDockerContainerName());
        if (containerName == null) {
            throw new Exception("Could not find the Docker container running PostgreSQL.");
        }

        String osInsideContainer = dockerService.getContainerOS(containerName);
        log.info("OS inside the Docker container: {}", osInsideContainer);

        boolean success;

        if (osInsideContainer.toLowerCase().contains("debian") || osInsideContainer.toLowerCase().contains("ubuntu")) {
            // Debian-based
            String[] commands = {
                    "apt-get update",
                    "apt-get install -y git build-essential postgresql-server-dev-all",
                    "git clone --branch v0.4.2 https://github.com/pgvector/pgvector /tmp/pgvector",
                    "cd /tmp/pgvector && make && make install"
            };
            success = dockerService.executeCommandsInContainer(containerName, commands);
        } else if (osInsideContainer.toLowerCase().contains("alpine")) {
            // Alpine-based
            String[] commands = {
                    "apk add --no-cache git build-base postgresql-dev",
                    "git clone --branch v0.4.2 https://github.com/pgvector/pgvector /tmp/pgvector",
                    "cd /tmp/pgvector && make && make install"
            };
            success = dockerService.executeCommandsInContainer(containerName, commands);
        } else if (osInsideContainer.toLowerCase().contains("arch") || osInsideContainer.toLowerCase().contains("manjaro")) {
            // Arch-based (Manjaro)
            String[] commands = {
                    "pacman -Syu --noconfirm",
                    "pacman -S --needed --noconfirm base-devel git postgresql-libs postgresql",
                    "git clone https://github.com/pgvector/pgvector.git /tmp/pgvector",
                    "cd /tmp/pgvector && make && make install"
            };
            success = dockerService.executeCommandsInContainer(containerName, commands);
        } else {
            throw new Exception("Unsupported OS inside the Docker container: " + osInsideContainer);
        }

        if (!success) {
            throw new Exception("Failed to install pgvector in the Docker container.");
        }
    }
}
