package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDefaults;
import org.craft.mcs.cap.core.account.provisioning.application.model.WorkspaceProvisioningDefaults;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsDefaults;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires account provisioning defaults from external CAP configuration. */
@Configuration
@EnableConfigurationProperties(AccountProperties.class)
public class AccountConfiguration {

  @Bean
  public @NonNull WorkspaceSettingsDefaults workspaceSettingsDefaults(
      @NonNull AccountProperties properties
  ) {
    final var settings = properties.defaultWorkspace().settings();
    return new WorkspaceSettingsDefaults(
        settings.measurementStaleAfter(),
        settings.defaultAcidityBasis()
    );
  }

  @Bean
  public @NonNull WorkspaceProvisioningDefaults workspaceProvisioningDefaults(
      @NonNull AccountProperties properties,
      @NonNull WorkspaceSettingsDefaults workspaceSettingsDefaults
  ) {
    final var defaultWorkspace = properties.defaultWorkspace();
    return new WorkspaceProvisioningDefaults(
        DisplayName.of(defaultWorkspace.name()),
        defaultWorkspace.type(),
        workspaceSettingsDefaults
    );
  }

  @Bean
  public @NonNull WorkspaceInvitationDefaults workspaceInvitationDefaults(
      @NonNull AccountProperties properties
  ) {
    return new WorkspaceInvitationDefaults(properties.invitationTimeToLive());
  }
}
