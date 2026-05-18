package org.craft.mcs.cap.shared.testing.jpa;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.craft.mcs.cap.shared.testing.container.TestContainerImages;
import org.testcontainers.postgresql.PostgreSQLContainer;


@TestConfiguration(proxyBeanMethods = false)
@EnableJpaAuditing
public class PostgresDataJpaTestConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer(TestContainerImages.POSTGRES);
  }
}
