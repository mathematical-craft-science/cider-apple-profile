package org.craft.mcs.cap;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.craft.mcs.cap.shared.testing.container.TestContainerImages;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  LgtmStackContainer grafanaLgtmContainer() {
    return new LgtmStackContainer(TestContainerImages.GRAFANA_LGTM);
  }

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer(TestContainerImages.POSTGRES);
  }

}
