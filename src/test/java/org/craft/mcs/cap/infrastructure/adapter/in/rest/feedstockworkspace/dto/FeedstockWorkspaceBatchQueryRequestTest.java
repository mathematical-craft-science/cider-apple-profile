package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FeedstockWorkspaceBatchQueryRequestTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final UUID WORKSPACE_ID = UUID.fromString("01900000-0000-7000-8000-000000000001");

  @Test
  void acceptsLegacyPurposeAliasButPublishesAnalysisScenario() throws Exception {
    final var request = OBJECT_MAPPER.readValue(
        """
            {
              "workspaceId": "%s",
              "purpose": "BLENDING_PLANNING"
            }
            """.formatted(WORKSPACE_ID),
        FeedstockWorkspaceBatchQueryRequest.class
    );
    final var serialized = OBJECT_MAPPER.valueToTree(request);

    assertThat(request.analysisScenario())
        .isEqualTo(ProfileTargetAnalysisScenario.BLENDING_PLANNING);
    assertThat(List.of(
        serialized.has("analysisScenario"),
        serialized.has("purpose")
    )).containsExactly(true, false);
  }
}
