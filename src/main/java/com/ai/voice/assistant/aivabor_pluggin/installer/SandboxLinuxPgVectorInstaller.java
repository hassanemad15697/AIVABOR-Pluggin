package com.ai.voice.assistant.aivabor_pluggin.installer;

import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Concrete strategy for installing pgvector in Sandbox Linux environment.
 */
@Component
@Slf4j
public class SandboxLinuxPgVectorInstaller implements PgVectorInstaller {

    @Override
    public void installPgVector(ProcessRequest request) throws Exception {
        String distro = getLinuxDistro();
        log.info("Linux distribution detected: {}", distro);

        boolean success;

        if (distro.toLowerCase().contains("debian") || distro.toLowerCase().contains("ubuntu")) {
            // Debian-based
            String[] commands = {
                    "sudo apt-get update",
                    "sudo apt-get install -y git build-essential postgresql-server-dev-all",
                    "git clone --branch v0.4.2 https://github.com/pgvector/pgvector /tmp/pgvector",
                    "cd /tmp/pgvector && make && sudo make install"
            };
            success = executeShellCommands(commands);
        } else if (distro.toLowerCase().contains("arch") || distro.toLowerCase().contains("manjaro")) {
            // Arch-based (Manjaro)
            String[] commands = {
                    "sudo pacman -Syu --noconfirm",
                    "sudo pacman -S --needed --noconfirm base-devel git postgresql-libs postgresql",
                    "git clone https://github.com/pgvector/pgvector.git /tmp/pgvector",
                    "cd /tmp/pgvector && make && sudo make install"
            };
            success = executeShellCommands(commands);
        } else {
            throw new Exception("Unsupported Linux distribution: " + distro);
        }

        if (!success) {
            throw new Exception("Failed to install pgvector on the host system.");
        }
    }

    /**
     * Executes shell commands on the host system.
     */
    private boolean executeShellCommands(String[] commands) {
        try {
            String command = String.join(" && ", commands);
            String[] cmd = {"/bin/sh", "-c", command};

            ProcessBuilder pb = new ProcessBuilder(cmd);
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
            log.error("Error executing shell commands: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the Linux distribution name.
     */
    private String getLinuxDistro() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ProcessBuilder("cat", "/etc/os-release").start().getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PRETTY_NAME=")) {
                    return line.split("=")[1].replace("\"", "");
                }
            }
        } catch (Exception e) {
            log.error("Failed to determine Linux distribution: {}", e.getMessage());
        }
        return "Unknown";
    }
}
