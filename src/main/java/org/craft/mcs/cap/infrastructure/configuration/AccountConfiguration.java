package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.account.application.model.WorkspaceProvisioningDefaults;
import org.craft.mcs.cap.core.account.application.model.WorkspaceSettingsDefaults;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/** Wires account provisioning defaults from external CAP configuration. */
@Configuration
@EnableConfigurationProperties(AccountProperties.class)
public class AccountConfiguration {

  @Bean
  public @NonNull WorkspaceProvisioningDefaults workspaceProvisioningDefaults(
      @NonNull AccountProperties properties
  ) {
    final var defaultWorkspace = properties.defaultWorkspace();
    final var defaultSettings = defaultWorkspace.settings();
    return new WorkspaceProvisioningDefaults(
        DisplayName.of(defaultWorkspace.name()),
        defaultWorkspace.type(),
        new WorkspaceSettingsDefaults(
            defaultSettings.measurementStaleAfter(),
            defaultSettings.defaultAcidityBasis()
        )
    );
  }
}
