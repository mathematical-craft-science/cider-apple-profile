package org.craft.mcs.cap.shared.testing.container;

import org.testcontainers.utility.DockerImageName;


/**
 * Shared Testcontainers image references used by local and slice tests.
 */
public final class TestContainerImages {

  public static final DockerImageName POSTGRES = DockerImageName.parse("postgres:latest");
  public static final DockerImageName GRAFANA_LGTM = DockerImageName.parse("grafana/otel-lgtm:latest");

  private TestContainerImages() {
  }
}
