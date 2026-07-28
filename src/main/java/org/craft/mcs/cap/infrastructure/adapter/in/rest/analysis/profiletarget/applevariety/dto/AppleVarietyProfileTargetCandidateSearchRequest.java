package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Search request for target-aware apple variety candidates.")
public record AppleVarietyProfileTargetCandidateSearchRequest(
    @Schema(
        description = "Optional case-insensitive name fragment filter.",
        example = "kingston",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String nameContains,
    @Schema(
        description = "Optional Jolicoeur sugar-acidity zone filter.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "PERFECT_APPLES",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable JolicoeurSugarAcidityZone zone,
    @Schema(
        description = "Optional chemical profile capability filter.",
        implementation = ChemicalProfileCapability.class,
        example = "SUGAR_ACIDITY_GRAPH",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalProfileCapability capability,
    @Schema(
        description = "Whether the optional capability must be met.",
        example = "true",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Boolean capable,
    @Schema(
        description = "Analysis scenario used to select scoring defaults.",
        implementation = ProfileTargetAnalysisScenario.class,
        example = "BLENDING_PLANNING",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonAlias("purpose")
    @NonNull ProfileTargetAnalysisScenario analysisScenario,
    @Schema(description = "Target to score candidates against.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetRequest target,
    @Schema(
        description = "Optional tannin target range.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(
        description = "Maximum results returned.",
        minimum = "1",
        maximum = "200",
        defaultValue = "50",
        example = "50",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Integer limit
) {

  public AppleVarietyProfileTargetCandidateSearchRequest {
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(target, "apple variety profile target cannot be null");
  }
}
