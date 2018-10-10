package et.docker;

import java.io.Serializable;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import et.utils.Shell;

public class DockerService implements Serializable {
    private static final Logger LOG = LoggerFactory
            .getLogger(DockerService.class);
    private static final long serialVersionUID = 1;
    public static final String DOCKER_HOST_BY_DEFAULT = "unix:///var/run/docker.sock";
    private static DockerService dockerService;

    private DockerService(String dockerHost) {
        if (dockerHost != null && !dockerHost.isEmpty()) {

        }
    }

    public synchronized static DockerService getDockerService(
            String dockerHost) {
        LOG.info("Retrieving docker service.");
        if (dockerService != null) {
            return dockerService;
        } else {
            return new DockerService(dockerHost);
        }
    }

    public String executeDockerCommand(String... startCommand) {
        LOG.info("Docker command to execute: {}",
                Arrays.toString(startCommand));
        String result = Shell.runAndWait(startCommand).replaceAll("\n", "");
        return result;
    }

    public String getGatewayFromContainer(String containerName) {
        String gateway = null;
        gateway = executeDockerCommand("docker", "inspect",
                "--format=\\\"{{.NetworkSettings.Networks.elastest_elastest.Gateway}}\\\"",
                containerName);
        LOG.info("Docker network gateway: {}", gateway);
        return gateway.replaceAll("\\\\\"", "");
    }
}
