package org.craft.mcs.cap.infrastructure.configuration;

import java.time.Duration;
import java.util.Map;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


class AccountPropertiesBindingTest {

  @Test
  void bindsNestedWorkspaceIdentityAndSettingsDefaults() {
    final var source = new MapConfigurationPropertySource(Map.of(
        "cap.account.default-workspace.name", "Research cidery",
        "cap.account.default-workspace.type", "INSTITUTE",
        "cap.account.default-workspace.settings.measurement-stale-after", "21d",
        "cap.account.default-workspace.settings.default-acidity-basis", "CITRIC_ACID"
    ));

    final var properties = new Binder(source)
        .bind("cap.account", Bindable.of(AccountProperties.class))
        .orElseThrow(AssertionError::new);

    assertThat(properties.defaultWorkspace())
        .returns("Research cidery", AccountProperties.DefaultWorkspaceProperties::name)
        .returns(WorkspaceType.INSTITUTE, AccountProperties.DefaultWorkspaceProperties::type)
        .returns(
            Duration.ofDays(21),
            value -> value.settings().measurementStaleAfter()
        )
        .returns(
            TitratableAcidityBasis.CITRIC_ACID,
            value -> value.settings().defaultAcidityBasis()
        );
  }
}
