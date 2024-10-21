package com.ai.voice.assistant.aivabor_pluggin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Service for Docker-related operations.
 */
@Service
@Slf4j
public class DockerService {

    /**
     * Retrieves the name of the Docker container running PostgreSQL.
     */
    public String getDockerContainerName(String containerFilter) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "ps", "--format", "{{.Names}}", "--filter", "name=" + containerFilter);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String containerName = reader.readLine();
            int exitCode = process.waitFor();
            if (exitCode != 0 || containerName == null || containerName.isEmpty()) {
                log.error("Failed to find a running PostgreSQL Docker container.");
                return null;
            }
            return containerName.trim();
        }
    }

    /**
     * Retrieves the OS information from the Docker container.
     */
    public String getContainerOS(String containerName) throws Exception {
        String command = String.format("docker exec %s cat /etc/os-release", containerName);
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder osRelease = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                osRelease.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new Exception("Failed to get OS information from the container.");
            }
            return parseOSName(osRelease.toString());
        }
    }

    /**
     * Executes commands inside the Docker container.
     */
    public boolean executeCommandsInContainer(String containerName, String[] commands) {
        try {
            String command = String.join(" && ", commands);
            String dockerExecCommand = String.format("docker exec -u root %s /bin/sh -c \"%s\"", containerName, command);

            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", dockerExecCommand);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("Shell command execution failed with exit code {}. Output:\n{}", exitCode, output.toString());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Error executing commands in Docker container: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses OS name from /etc/os-release content.
     */
    private String parseOSName(String osReleaseContent) {
        if (osReleaseContent.contains("PRETTY_NAME=")) {
            int index = osReleaseContent.indexOf("PRETTY_NAME=");
            int endIndex = osReleaseContent.indexOf("\n", index);
            String prettyName = osReleaseContent.substring(index + "PRETTY_NAME=".length(), endIndex).replace("\"", "");
            return prettyName;
        } else if (osReleaseContent.contains("ID=")) {
            int index = osReleaseContent.indexOf("ID=");
            int endIndex = osReleaseContent.indexOf("\n", index);
            String id = osReleaseContent.substring(index + "ID=".length(), endIndex).replace("\"", "");
            return id;
        } else {
            return "Unknown";
        }
    }
}
