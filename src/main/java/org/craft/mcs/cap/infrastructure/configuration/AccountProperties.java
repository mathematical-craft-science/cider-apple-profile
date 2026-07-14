package org.craft.mcs.cap.infrastructure.configuration;

import java.time.Duration;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static java.util.Objects.requireNonNull;

/** External defaults used when CAP creates the first workspace for an account. */
@ConfigurationProperties(prefix = "cap.account")
public record AccountProperties(
    /* Defaults used when CAP lazily provisions the first workspace for an account. */
    @NonNull DefaultWorkspaceProperties defaultWorkspace,
    /* Validity period assigned to a new or resent workspace invitation. */
    @NonNull Duration invitationTimeToLive
) {

  public AccountProperties(
      @NonNull DefaultWorkspaceProperties defaultWorkspace,
      @DefaultValue("7d") @NonNull Duration invitationTimeToLive
  ) {
    this.defaultWorkspace = requireNonNull(
        defaultWorkspace,
        "default workspace properties cannot be null"
    );
    this.invitationTimeToLive = requireNonNull(
        invitationTimeToLive,
        "workspace invitation time to live cannot be null"
    );
    if (invitationTimeToLive.isZero() || invitationTimeToLive.isNegative()) {
      throw new IllegalArgumentException("workspace invitation time to live must be positive");
    }
  }

  /** Default identity of a lazily provisioned CAP workspace. */
  public record DefaultWorkspaceProperties(
      /* Human-readable name assigned to the provisioned workspace. */
      @NonNull String name,
      /* Business type assigned to the provisioned workspace. */
      @NonNull WorkspaceType type,
      /* Product settings copied into the provisioned workspace. */
      @NonNull DefaultWorkspaceSettingsProperties settings
  ) {

    public DefaultWorkspaceProperties(
        @DefaultValue("My cidery") @NonNull String name,
        @DefaultValue("CIDERY") @NonNull WorkspaceType type,
        @NonNull DefaultWorkspaceSettingsProperties settings
    ) {
      this.name = requireNonNull(name, "default workspace name cannot be null");
      this.type = requireNonNull(type, "default workspace type cannot be null");
      this.settings = requireNonNull(settings, "default workspace settings cannot be null");
    }
  }

  /** Product settings copied into a lazily provisioned CAP workspace. */
  public record DefaultWorkspaceSettingsProperties(
      /* Age after which a measurement is considered stale in the provisioned workspace. */
      @NonNull Duration measurementStaleAfter,
      /* Canonical titratable-acidity basis selected for the provisioned workspace. */
      @NonNull TitratableAcidityBasis defaultAcidityBasis
  ) {

    public DefaultWorkspaceSettingsProperties(
        @NonNull Duration measurementStaleAfter,
        @DefaultValue("MALIC_ACID") @NonNull TitratableAcidityBasis defaultAcidityBasis
    ) {
      this.measurementStaleAfter = requireNonNull(
          measurementStaleAfter,
          "default workspace measurement stale duration cannot be null"
      );
      if (measurementStaleAfter.isZero() || measurementStaleAfter.isNegative()) {
        throw new IllegalArgumentException("default workspace measurement stale duration must be positive");
      }
      this.defaultAcidityBasis = requireNonNull(
          defaultAcidityBasis,
          "default acidity basis cannot be null"
      );
    }
  }
}
