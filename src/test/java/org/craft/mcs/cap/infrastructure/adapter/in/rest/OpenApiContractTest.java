package org.craft.mcs.cap.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.craft.mcs.cap.TestcontainersConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;


@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiContractTest {

  @LocalServerPort
  private int port;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void openApiDescribesJsonMediaTypesAndStrongDtoSchemas() throws Exception {
    final var openApi = openApi();

    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/classification", "get");
    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/north-american", "get");
    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/abv-prediction", "get");
    assertJsonResponse(openApi, "/apple-varieties/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/analysis/apple-varieties/profile-target-candidates", "post");
    assertJsonResponse(openApi, "/analysis/feedstock-batches/profile-target-candidates", "post");
    assertJsonResponse(openApi, "/chemical-profiles/sugar-acidity-zone/plot-metadata", "get");
    assertJsonResponse(openApi, "/measurement-units/metadata", "get");
    assertJsonResponse(openApi, "/chemical-profile-compositions/calculations", "post");
    assertJsonResponse(openApi, "/chemical-profile-compositions/optimizations", "post");
    assertJsonResponse(openApi, "/feedstock-batches", "get");
    assertJsonResponse(openApi, "/feedstock-batches", "post", "201");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}", "put");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}", "patch");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements", "get");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements", "post", "201");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "put");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "patch");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/feedstock-batches/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/abv-prediction", "get");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/fermentation-start-readiness", "get");
    assertJsonResponse(openApi, "/feedstock-batches/profile-target-scores", "post");
    assertJsonResponse(openApi, "/feedstock-batches/composition-optimizations", "post");
    assertJsonResponse(openApi, "/feedstock-workspace/batches", "post");
    assertJsonResponse(openApi, "/feedstock-measurements/{measurementId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/feedstock-measurements/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/feedstock-measurements/{measurementId}/abv-prediction", "get");
    assertJsonResponse(openApi, "/production-batches", "get");
    assertJsonResponse(openApi, "/production-batches", "post", "201");
    assertJsonResponse(openApi, "/production-batches/{batchId}", "get");
    assertJsonResponse(openApi, "/production-batches/{batchId}/traceability", "get");

    assertJsonRequest(openApi, "/feedstock-batches", "post");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}", "put");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}", "patch");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}/measurements", "post");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "put");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "patch");
    assertJsonRequest(openApi, "/apple-varieties/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/analysis/apple-varieties/profile-target-candidates", "post");
    assertJsonRequest(openApi, "/analysis/feedstock-batches/profile-target-candidates", "post");
    assertJsonRequest(openApi, "/chemical-profile-compositions/calculations", "post");
    assertJsonRequest(openApi, "/chemical-profile-compositions/optimizations", "post");
    assertJsonRequest(openApi, "/feedstock-batches/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/feedstock-batches/profile-target-scores", "post");
    assertJsonRequest(openApi, "/feedstock-batches/composition-optimizations", "post");
    assertJsonRequest(openApi, "/feedstock-workspace/batches", "post");
    assertJsonRequest(openApi, "/feedstock-measurements/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/production-batches", "post");

    assertStringFormat(schemaProperty(openApi, "AppleVarietyClassificationResponse", "varietyId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchResponse", "batchId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockMeasurementResponse", "measuredAt"), "date-time");
    assertStringFormat(schemaProperty(openApi, "FeedstockSeasonResponse", "startsOn"), "date");
    assertArray(schemaProperty(openApi, "AppleVarietySugarAcidityZonePointBatchResponse", "points"));
    assertArray(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointBatchResponse", "points"));
    assertArray(schemaProperty(openApi, "FeedstockMeasurementSugarAcidityZonePointBatchResponse", "unplottable"));
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "batchId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "ciderMakerId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "currentMeasurementId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "measuredAt"), "date-time");
    assertArray(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "missingAxes"));
    assertRequiredProperties(openApi, "FeedstockBatchGraphPointResponse",
        "batchId", "batchName", "ciderMakerId", "currentMeasurementId", "measuredAt", "plottable", "missingAxes");
    assertNotRequiredProperties(openApi, "FeedstockBatchGraphPointResponse",
        "point", "zone", "assessment", "capabilityAssessment");
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "point"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "zone"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "assessment"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "capabilityAssessment"));

    assertEnumProperty(openApi, "CreateFeedstockBatchRequest", "type", "APPLE_JUICE");
    assertEnumProperty(openApi, "ChemicalProfileCapabilityAssessmentResponse", "capability", "SUGAR_ACIDITY_PLOT");
    assertEnumProperty(openApi, "PotentialAbvPredictionResponse", "confidence", "LOW");
    assertNoLegacyPhysicalQuantityResponseFields(openApi);
    assertNoBareNumericResponsePropertiesOutsideAllowList(openApi);
    assertRequiredProperties(
        openApi,
        "PotentialAbvPredictionResponse",
        "originalGravity",
        "predictedFinalGravity",
        "predictedFinalGravitySource",
        "potentialAbv",
        "method",
        "confidence"
    );
    assertRequiredProperties(openApi, "DecimalQuantityResponse", "value", "unit");

    assertRequiredProperties(
        openApi,
        "CreateFeedstockBatchRequest",
        "ciderMakerId",
        "name",
        "type",
        "season",
        "availableVolumeLiters"
    );
    assertNotRequiredProperties(openApi, "CreateFeedstockBatchRequest", "source", "note");
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "source"));
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "note"));

    assertRequiredProperties(
        openApi,
        "CreateFeedstockMeasurementRequest",
        "name",
        "chemicalProfile",
        "measuredAt"
    );
    assertNotRequiredProperties(openApi, "CreateFeedstockMeasurementRequest", "note");
    assertNullable(schemaProperty(openApi, "CreateFeedstockMeasurementRequest", "note"));

    assertRequiredProperties(
        openApi,
        "FeedstockMeasurementResponse",
        "measurementId",
        "ciderMakerId",
        "batchId",
        "name"
    );
    assertNotRequiredProperties(openApi, "FeedstockMeasurementResponse", "measuredAt", "chemicalProfile", "note");
    assertNullable(schemaProperty(openApi, "FeedstockMeasurementResponse", "measuredAt"));
    assertNullable(schemaProperty(openApi, "FeedstockMeasurementResponse", "chemicalProfile"));
    assertNullable(schemaProperty(openApi, "FeedstockMeasurementResponse", "note"));

    assertRequiredProperties(openApi, "SugarAcidityProfilePointResponse", "sugar", "acidity");
    assertNotRequiredProperties(openApi, "SugarAcidityProfilePointResponse", "tannin");
    assertNullable(schemaProperty(openApi, "SugarAcidityProfilePointResponse", "tannin"));

    assertRequiredProperties(
        openApi,
        "ProfileTargetDistanceResponse",
        "status",
        "insideSugarAcidityTarget",
        "insideTarget"
    );
    assertRequiredProperties(openApi, "SugarAcidityCoordinatesResponse", "sugar", "acidity");
    assertNotRequiredProperties(
        openApi,
        "ProfileTargetDistanceResponse",
        "value",
        "tannin",
        "nearestTargetTannin"
    );
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "value"));
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "nearestTargetTannin"));

    assertRequiredProperties(openApi, "SugarAcidityGraphRangeResponse", "min", "max");
    assertRequiredProperties(
        openApi,
        "SugarAcidityGraphAxisResponse",
        "code",
        "label",
        "unit",
        "min",
        "max",
        "thresholdLines"
    );
    assertRequiredProperties(openApi, "MeasurementUnitsMetadataResponse", "units");
    assertRequiredProperties(
        openApi,
        "MeasurementUnitMetadataResponse",
        "code",
        "canonicalUnit",
        "shortLabel",
        "fullLabel",
        "min",
        "max",
        "step"
    );
    assertEnumProperty(openApi, "MeasurementUnitMetadataResponse", "code", "SG");
    assertRequiredProperties(
        openApi,
        "EllipseSugarAcidityGraphZoneResponse",
        "zone",
        "sugarCenter",
        "acidityCenter",
        "sugarRadius",
        "acidityRadius"
    );

    assertRequiredProperties(
        openApi,
        "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest",
        "ciderMakerId"
    );
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest",
        "batchId",
        "season",
        "after",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "batchId"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "season"));

    assertParameterRequired(openApi, "/feedstock-batches", "get", "ciderMakerId", true);
    assertParameterRequired(openApi, "/feedstock-batches", "get", "seasonStartsOn", false);
    assertParameterRequired(openApi, "/feedstock-batches", "get", "seasonEndsOn", false);

    assertOneOfSchema(openApi, "ChemicalMeasurementRequest");
    assertNoAllOfSchema(openApi, "ExactChemicalMeasurementRequest");
    assertNoAllOfSchema(openApi, "RangeChemicalMeasurementRequest");
    assertNoAllOfSchema(openApi, "StatisticalChemicalMeasurementRequest");
    assertOneOfSchema(openApi, "ChemicalMeasurementResponse");
    assertNoAllOfSchema(openApi, "ExactChemicalMeasurementResponse");
    assertNoAllOfSchema(openApi, "RangeChemicalMeasurementResponse");
    assertNoAllOfSchema(openApi, "StatisticalChemicalMeasurementResponse");
    assertRequiredProperties(openApi, "ExactChemicalMeasurementResponse", "kind", "value");
    assertRequiredProperties(openApi, "RangeChemicalMeasurementResponse", "kind", "min", "max");
    assertRequiredProperties(openApi, "StatisticalChemicalMeasurementResponse", "kind", "mean");
    assertNotRequiredProperties(openApi, "ExactChemicalMeasurementResponse", "unit");
    assertNotRequiredProperties(openApi, "RangeChemicalMeasurementResponse", "unit");
    assertNotRequiredProperties(openApi, "StatisticalChemicalMeasurementResponse", "unit");
    assertOneOfSchema(openApi, "ProfileTargetRequest");
    assertNoAllOfSchema(openApi, "PredefinedZoneProfileTargetRequest");
    assertNoAllOfSchema(openApi, "CustomRectangleProfileTargetRequest");
    assertOneOfSchema(openApi, "SugarAcidityAxisPointResponse");
    assertNoAllOfSchema(openApi, "ExactSugarAcidityAxisPointResponse");
    assertNoAllOfSchema(openApi, "RangeSugarAcidityAxisPointResponse");
    assertNoAllOfSchema(openApi, "StatisticalSugarAcidityAxisPointResponse");
    assertNoAllOfSchema(openApi, "CalculatedSugarAcidityAxisPointResponse");
    assertOneOfSchema(openApi, "SugarAcidityGraphZoneResponse");
    assertNoAllOfSchema(openApi, "RectangleSugarAcidityGraphZoneResponse");
    assertNoAllOfSchema(openApi, "EllipseSugarAcidityGraphZoneResponse");

    assertRequiredProperties(openApi, "ChemicalProfileCompositionResponse", "sources", "profile", "warnings");
    assertNotRequiredProperties(
        openApi,
        "ChemicalProfileCompositionResponse",
        "yeastAssimilableNitrogen",
        "totalVolume",
        "normalizerCode",
        "targetDistance"
    );
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionResponse", "yeastAssimilableNitrogen"));
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionResponse", "totalVolume"));
    assertRequiredProperties(openApi, "ChemicalProfileCompositionSourceResponse", "reference");
    assertNotRequiredProperties(openApi, "ChemicalProfileCompositionSourceResponse", "requestedVolume", "proportion");
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionSourceResponse", "requestedVolume"));
    assertRequiredProperties(
        openApi,
        "ChemicalProfileCompositionOptimizationCandidateResponse",
        "sources",
        "profile",
        "warnings",
        "targetDistance"
    );
    assertNotRequiredProperties(
        openApi,
        "ChemicalProfileCompositionOptimizationCandidateResponse",
        "yeastAssimilableNitrogen",
        "totalVolume"
    );
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionOptimizationCandidateResponse", "totalVolume"));
    assertNoSchemaComposesDiscriminatedUnionThroughAllOf(openApi);
  }

  @Test
  void openApiDescribesAnalysisProfileTargetRequiredNullableContracts() throws Exception {
    final var openApi = openApi();

    assertRequiredProperties(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "purpose", "target");
    assertNotRequiredProperties(
        openApi,
        "AppleVarietyProfileTargetCandidateSearchRequest",
        "nameContains",
        "zone",
        "capability",
        "capable",
        "tanninTarget",
        "weights",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "nameContains"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "zone"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "capability"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "capable"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "tanninTarget"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "weights"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "limit"));

    assertRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidateSearchRequest",
        "ciderMakerId",
        "purpose",
        "target"
    );
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidateSearchRequest",
        "batchIds",
        "season",
        "zone",
        "capability",
        "capable",
        "tanninTarget",
        "weights",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "batchIds"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "season"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "zone"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "capability"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "capable"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "tanninTarget"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "weights"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "limit"));

    assertRequiredProperties(
        openApi,
        "AppleVarietyProfileTargetCandidatesResponse",
        "purpose",
        "normalizerCode",
        "candidates"
    );
    assertArray(schemaProperty(openApi, "AppleVarietyProfileTargetCandidatesResponse", "candidates"));
    assertRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidatesResponse",
        "purpose",
        "normalizerCode",
        "candidates"
    );
    assertArray(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidatesResponse", "candidates"));

    assertRequiredProperties(
        openApi,
        "AppleVarietyProfileTargetCandidateResponse",
        "varietyId",
        "varietyName",
        "point",
        "distance",
        "contributionRole"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidateResponse",
        "batchId",
        "currentMeasurementId",
        "currentMeasurementName",
        "point",
        "distance",
        "contributionRole"
    );
    assertRequiredProperties(openApi, "CompositionContributionEvidenceResponse",
        "axis", "sourceValue", "targetCoordinate", "deviationFromTarget", "role");
    assertRequiredProperties(openApi, "CompositionContributionRoleAssessmentResponse",
        "roles", "primaryRole", "evidence", "missingAxes", "assessable");
    assertArray(schemaProperty(openApi, "CompositionContributionRoleAssessmentResponse", "roles"));
    assertArray(schemaProperty(openApi, "CompositionContributionRoleAssessmentResponse", "evidence"));
    assertArray(schemaProperty(openApi, "CompositionContributionRoleAssessmentResponse", "missingAxes"));
  }

  @Test
  void openApiDescribesProductionAndFeedstockRequiredNullableContracts() throws Exception {
    final var openApi = openApi();

    assertRequiredProperties(
        openApi,
        "CreateProductionBatchRequest",
        "ciderMakerId",
        "name",
        "kind",
        "sources",
        "profile"
    );
    assertNotRequiredProperties(openApi, "CreateProductionBatchRequest", "targetDistance", "note");
    assertNullable(schemaProperty(openApi, "CreateProductionBatchRequest", "targetDistance"));
    assertNullable(schemaProperty(openApi, "CreateProductionBatchRequest", "note"));
    assertRequiredProperties(openApi, "CreateProductionBatchSourceRequest", "feedstockBatchId", "volumeLiters");
    assertRequiredProperties(openApi, "ProductionProfileSnapshotRequest", "sugar", "acidity");
    assertNotRequiredProperties(openApi, "ProductionProfileSnapshotRequest", "tannin", "yeastAssimilableNitrogen");
    assertNullable(schemaProperty(openApi, "ProductionProfileSnapshotRequest", "tannin"));
    assertNullable(schemaProperty(openApi, "ProductionProfileSnapshotRequest", "yeastAssimilableNitrogen"));

    assertRequiredProperties(
        openApi,
        "ProductionBatchDetailsResponse",
        "batchId",
        "ciderMakerId",
        "name",
        "kind",
        "status",
        "volume",
        "profile",
        "potentialAbv"
    );
    assertNotRequiredProperties(
        openApi,
        "ProductionBatchDetailsResponse",
        "yeastAssimilableNitrogen",
        "targetDistance",
        "note"
    );
    assertNullable(schemaProperty(openApi, "ProductionBatchDetailsResponse", "yeastAssimilableNitrogen"));
    assertNullable(schemaProperty(openApi, "ProductionBatchDetailsResponse", "targetDistance"));
    assertNullable(schemaProperty(openApi, "ProductionBatchDetailsResponse", "note"));

    assertRequiredProperties(
        openApi,
        "ProductionBatchResponse",
        "batchId",
        "ciderMakerId",
        "name",
        "kind",
        "status",
        "composition"
    );
    assertNotRequiredProperties(openApi, "ProductionBatchResponse", "note");
    assertNullable(schemaProperty(openApi, "ProductionBatchResponse", "note"));
    assertRequiredProperties(openApi, "ProductionBatchCompositionResponse", "sources", "profile", "totalVolume");
    assertNotRequiredProperties(
        openApi,
        "ProductionBatchCompositionResponse",
        "yeastAssimilableNitrogen",
        "targetDistance"
    );
    assertNullable(schemaProperty(openApi, "ProductionBatchCompositionResponse", "yeastAssimilableNitrogen"));
    assertNullable(schemaProperty(openApi, "ProductionBatchCompositionResponse", "targetDistance"));
    assertRequiredProperties(openApi, "ProductionBatchSourceResponse", "feedstockBatchId", "volume");

    assertRequiredProperties(openApi, "BatchTraceabilityGraphResponse", "nodes", "edges");
    assertRequiredProperties(openApi, "BatchTraceNodeResponse", "subject");
    assertNotRequiredProperties(openApi, "BatchTraceNodeResponse", "snapshot");
    assertNullable(schemaProperty(openApi, "BatchTraceNodeResponse", "snapshot"));
    assertRequiredProperties(
        openApi,
        "BatchTraceEdgeResponse",
        "stepId",
        "operationType",
        "occurredAt",
        "inputs",
        "outputs"
    );
    assertStringFormat(schemaProperty(openApi, "BatchTraceEdgeResponse", "occurredAt"), "date-time");
    assertRequiredProperties(openApi, "BatchTraceStepSubjectResponse", "subject");
    assertNotRequiredProperties(openApi, "BatchTraceStepSubjectResponse", "snapshot");
    assertNullable(schemaProperty(openApi, "BatchTraceStepSubjectResponse", "snapshot"));
    assertRequiredProperties(openApi, "BatchTraceSubjectResponse", "type", "id");
    assertNotRequiredProperties(openApi, "BatchTraceSubjectResponse", "displayNameSnapshot");
    assertNullable(schemaProperty(openApi, "BatchTraceSubjectResponse", "displayNameSnapshot"));
    assertRequiredProperties(openApi, "BatchTraceMaterialSnapshotResponse", "axes");
    assertNotRequiredProperties(openApi, "BatchTraceMaterialSnapshotResponse", "volume", "confidence");
    assertNullable(schemaProperty(openApi, "BatchTraceMaterialSnapshotResponse", "volume"));
    assertNullable(schemaProperty(openApi, "BatchTraceMaterialSnapshotResponse", "confidence"));
    assertRequiredProperties(openApi, "BatchTraceAxisValueResponse", "axis", "value", "valueKind");
    assertNotRequiredProperties(
        openApi,
        "BatchTraceAxisValueResponse",
        "min",
        "max",
        "standardDeviation",
        "sampleSize"
    );
    assertNullable(schemaProperty(openApi, "BatchTraceAxisValueResponse", "min"));
    assertNullable(schemaProperty(openApi, "BatchTraceAxisValueResponse", "max"));
    assertNullable(schemaProperty(openApi, "BatchTraceAxisValueResponse", "standardDeviation"));
    assertNullable(schemaProperty(openApi, "BatchTraceAxisValueResponse", "sampleSize"));

    assertRequiredProperties(
        openApi,
        "UpdateFeedstockBatchRequest",
        "ciderMakerId",
        "name",
        "type",
        "season",
        "availableVolumeLiters"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchResponse",
        "batchId",
        "ciderMakerId",
        "name",
        "type",
        "season",
        "availableVolume"
    );
    assertNotRequiredProperties(openApi, "UpdateFeedstockBatchRequest", "source", "note");
    assertNullable(schemaProperty(openApi, "UpdateFeedstockBatchRequest", "source"));
    assertNullable(schemaProperty(openApi, "UpdateFeedstockBatchRequest", "note"));
    assertRequiredProperties(openApi, "PatchFeedstockBatchRequest", "ciderMakerId");
    assertNotRequiredProperties(
        openApi,
        "PatchFeedstockBatchRequest",
        "name",
        "type",
        "season",
        "source",
        "availableVolumeLiters",
        "note"
    );
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "name"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "season"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "note"));

    assertRequiredProperties(openApi, "UpdateFeedstockMeasurementRequest", "name", "chemicalProfile", "measuredAt");
    assertNotRequiredProperties(openApi, "UpdateFeedstockMeasurementRequest", "note");
    assertNullable(schemaProperty(openApi, "UpdateFeedstockMeasurementRequest", "note"));
    assertNotRequiredProperties(
        openApi,
        "PatchFeedstockMeasurementRequest",
        "name",
        "chemicalProfile",
        "measuredAt",
        "note"
    );
    assertNullable(schemaProperty(openApi, "PatchFeedstockMeasurementRequest", "name"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockMeasurementRequest", "chemicalProfile"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockMeasurementRequest", "measuredAt"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockMeasurementRequest", "note"));

    assertRequiredProperties(openApi, "FeedstockBatchCurrentFermentationStartReadinessResponse",
        "batchId", "ciderMakerId", "currentMeasurementId", "currentMeasurementName", "assessment");
    assertNotRequiredProperties(openApi, "FermentationStartReadinessCheckResponse", "value", "unit");
    assertNullable(schemaProperty(openApi, "FermentationStartReadinessCheckResponse", "value"));
    assertRequiredProperties(
        openApi,
        "FeedstockBatchCompositionOptimizationCandidateResponse",
        "sources",
        "profile",
        "warnings",
        "targetDistance"
    );
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchCompositionOptimizationCandidateResponse",
        "yeastAssimilableNitrogen",
        "totalVolume"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCompositionOptimizationCandidateResponse", "totalVolume"));
    assertRequiredProperties(openApi, "FeedstockBatchCompositionOptimizationSourceResponse", "batchId");
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchCompositionOptimizationSourceResponse",
        "requestedVolume",
        "proportion"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCompositionOptimizationSourceResponse", "requestedVolume"));
  }

  @Test
  void openApiDescribesFeedstockWorkspaceRequiredNullableContracts() throws Exception {
    final var openApi = openApi();

    assertRequiredProperties(openApi, "FeedstockWorkspaceBatchQueryRequest", "ciderMakerId");
    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceBatchQueryRequest",
        "season",
        "target",
        "tanninTarget",
        "weights",
        "purpose",
        "targetCandidateLimit"
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "season"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "target"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "tanninTarget"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "weights"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "purpose"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "targetCandidateLimit"));

    assertRequiredProperties(openApi, "FeedstockWorkspaceResponse", "batches");
    assertArray(schemaProperty(openApi, "FeedstockWorkspaceResponse", "batches"));
    assertNotRequiredProperties(openApi, "FeedstockWorkspaceResponse", "targetAnalysis");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceResponse", "targetAnalysis"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceBatchResponse",
        "batch",
        "planningReadiness",
        "dataQuality"
    );
    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceBatchResponse",
        "currentMeasurement",
        "profileSummary",
        "measurementFreshness",
        "sugarAcidity",
        "potentialAbv",
        "yeastAssimilableNitrogen",
        "fermentationStartReadiness",
        "targetCandidate"
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "currentMeasurement"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "profileSummary"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "measurementFreshness"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "sugarAcidity"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "potentialAbv"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "yeastAssimilableNitrogen"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "fermentationStartReadiness"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "targetCandidate"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceCurrentMeasurementResponse",
        "measurementId",
        "name",
        "measuredAt",
        "chemicalProfile"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "measurementId"),
        "uuid"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "measuredAt"),
        "date-time"
    );
    assertNotRequiredProperties(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "note");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "note"));

    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceProfileSummaryResponse",
        "sugar",
        "titratableAcidity",
        "ph",
        "tannin",
        "yan"
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "sugar"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "titratableAcidity"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "ph"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "yan"));

    assertRequiredProperties(openApi, "FeedstockWorkspaceProfileValueResponse", "value", "valueKind");
    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceProfileValueResponse",
        "min",
        "max",
        "standardDeviation",
        "sampleSize"
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileValueResponse", "min"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileValueResponse", "max"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileValueResponse", "standardDeviation"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileValueResponse", "sampleSize"));

    assertRequiredProperties(openApi, "FeedstockWorkspaceMeasurementFreshnessResponse", "status", "stale");
    assertNotRequiredProperties(openApi, "FeedstockWorkspaceMeasurementFreshnessResponse", "reason");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceMeasurementFreshnessResponse", "reason"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceSugarAcidityResponse",
        "plottable",
        "capabilityAssessment"
    );
    assertNotRequiredProperties(openApi, "FeedstockWorkspaceSugarAcidityResponse", "point", "assessment");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceSugarAcidityResponse", "point"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceSugarAcidityResponse", "assessment"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspacePotentialAbvResponse",
        "originalGravityValueKind",
        "prediction"
    );
    assertRequiredProperties(openApi, "FeedstockWorkspaceTargetAnalysisResponse", "purpose", "normalizerCode");
    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceTargetCandidateResponse",
        "point",
        "distance",
        "contributionRole",
        "roleExplanation"
    );
    assertRequiredProperties(openApi, "FeedstockWorkspaceRoleExplanationResponse", "summary", "reasons");
    assertArray(schemaProperty(openApi, "FeedstockWorkspaceRoleExplanationResponse", "reasons"));
    assertRequiredProperties(openApi, "FeedstockWorkspacePlanningReadinessResponse", "status", "checks");
    assertArray(schemaProperty(openApi, "FeedstockWorkspacePlanningReadinessResponse", "checks"));
    assertRequiredProperties(
        openApi,
        "FeedstockWorkspacePlanningReadinessCheckResponse",
        "factor",
        "severity",
        "code",
        "message"
    );
    assertRequiredProperties(openApi, "FeedstockWorkspaceDataQualityResponse", "status", "indicators");
    assertArray(schemaProperty(openApi, "FeedstockWorkspaceDataQualityResponse", "indicators"));
    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceDataQualityIndicatorResponse",
        "code",
        "severity",
        "message"
    );
    assertEnumSchema(openApi, "FeedstockPlanningSignalSeverity", "OK", "INFO", "WARNING", "BLOCKING");
    assertMissingSchema(openApi, "FeedstockPlanningReadinessSeverity");
    assertMissingSchema(openApi, "FeedstockPlanningDataQualitySeverity");
    assertPropertyReferencesSchema(
        openApi,
        "FeedstockWorkspacePlanningReadinessCheckResponse",
        "severity",
        "FeedstockPlanningSignalSeverity"
    );
    assertPropertyReferencesSchema(
        openApi,
        "FeedstockWorkspaceDataQualityIndicatorResponse",
        "severity",
        "FeedstockPlanningSignalSeverity"
    );
  }

  private JsonNode openApi() throws Exception {
    final var response = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs"))
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString()
    );

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(contentType ->
            assertThat(MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue()
        );

    return objectMapper.readTree(response.body());
  }

  private static void assertJsonResponse(
      JsonNode openApi,
      String path,
      String method
  ) {
    assertJsonResponse(openApi, path, method, "200");
  }

  private static void assertJsonResponse(
      JsonNode openApi,
      String path,
      String method,
      String status
  ) {
    final var content = operation(openApi, path, method)
        .path("responses")
        .path(status)
        .path("content");

    assertThat(content.has("application/json"))
        .as("%s %s response content type", method, path)
        .isTrue();
    assertThat(content.has("*/*"))
        .as("%s %s wildcard response content type", method, path)
        .isFalse();
  }

  private static void assertJsonRequest(
      JsonNode openApi,
      String path,
      String method
  ) {
    final var content = operation(openApi, path, method)
        .path("requestBody")
        .path("content");

    assertThat(content.has("application/json"))
        .as("%s %s request content type", method, path)
        .isTrue();
    assertThat(content.has("*/*"))
        .as("%s %s wildcard request content type", method, path)
        .isFalse();
  }

  private static JsonNode operation(
      JsonNode openApi,
      String path,
      String method
  ) {
    final var operation = openApi.path("paths").path(path).path(method);
    assertThat(operation.isMissingNode())
        .as("%s %s operation exists", method, path)
        .isFalse();
    return operation;
  }

  private static JsonNode schemaProperty(
      JsonNode openApi,
      String schemaName,
      String propertyName
  ) {
    final var property = openApi
        .path("components")
        .path("schemas")
        .path(schemaName)
        .path("properties")
        .path(propertyName);

    assertThat(property.isMissingNode())
        .as("%s.%s exists", schemaName, propertyName)
        .isFalse();
    return property;
  }

  private static void assertMissingSchema(
      JsonNode openApi,
      String schemaName
  ) {
    final var schema = openApi
        .path("components")
        .path("schemas")
        .path(schemaName);

    assertThat(schema.isMissingNode())
        .as("%s schema should not be generated", schemaName)
        .isTrue();
  }

  private static void assertPropertyReferencesSchema(
      JsonNode openApi,
      String schemaName,
      String propertyName,
      String expectedSchemaName
  ) {
    final var property = schemaProperty(openApi, schemaName, propertyName);

    assertThat(referencedSchemaName(property))
        .as("%s.%s schema reference", schemaName, propertyName)
        .isEqualTo(expectedSchemaName);
  }

  private static void assertEnumSchema(
      JsonNode openApi,
      String schemaName,
      String... values
  ) {
    final var schema = openApi
        .path("components")
        .path("schemas")
        .path(schemaName);

    assertThat(schema.isMissingNode())
        .as("%s schema exists", schemaName)
        .isFalse();
    assertThat(schema.path("type").asText())
        .as("%s schema type", schemaName)
        .isEqualTo("string");
    assertThat(StreamSupport.stream(schema.path("enum").spliterator(), false)
        .map(JsonNode::asText)
        .toList())
        .as("%s enum values", schemaName)
        .containsExactly(values);
  }

  private static String referencedSchemaName(JsonNode schema) {
    final var ref = schema.path("$ref").asText(null);
    assertThat(ref)
        .as("schema reference")
        .isNotBlank();
    return ref.substring(ref.lastIndexOf('/') + 1);
  }

  private static void assertRequiredProperties(
      JsonNode openApi,
      String schemaName,
      String... propertyNames
  ) {
    final var required = requiredProperties(openApi, schemaName);
    assertThat(required).contains(propertyNames);
  }

  private static void assertNotRequiredProperties(
      JsonNode openApi,
      String schemaName,
      String... propertyNames
  ) {
    final var required = requiredProperties(openApi, schemaName);
    assertThat(required).doesNotContain(propertyNames);
  }

  private static List<String> requiredProperties(
      JsonNode openApi,
      String schemaName
  ) {
    return StreamSupport.stream(openApi
            .path("components")
            .path("schemas")
            .path(schemaName)
            .path("required")
            .spliterator(), false)
        .map(JsonNode::asText)
        .toList();
  }

  private static void assertNullable(JsonNode property) {
    final var nullable = property.path("nullable").asBoolean(false)
        || hasType(property, "null")
        || StreamSupport.stream(property.path("anyOf").spliterator(), false)
        .anyMatch(schema -> hasType(schema, "null"));

    assertThat(nullable).isTrue();
  }

  private static void assertParameterRequired(
      JsonNode openApi,
      String path,
      String method,
      String parameterName,
      boolean required
  ) {
    final var parameter = StreamSupport.stream(operation(openApi, path, method)
            .path("parameters")
            .spliterator(), false)
        .filter(candidate -> parameterName.equals(candidate.path("name").asText()))
        .findFirst();

    assertThat(parameter)
        .as("%s %s parameter %s exists", method, path, parameterName)
        .isPresent();
    assertThat(parameter.orElseThrow().path("required").asBoolean(false))
        .as("%s %s parameter %s required", method, path, parameterName)
        .isEqualTo(required);
  }

  private static void assertOneOfSchema(
      JsonNode openApi,
      String schemaName
  ) {
    final var schema = openApi.path("components").path("schemas").path(schemaName);
    assertThat(schema.path("oneOf").isArray())
        .as("%s should remain a discriminated oneOf union", schemaName)
        .isTrue();
  }

  private static void assertNoAllOfSchema(
      JsonNode openApi,
      String schemaName
  ) {
    final var schema = openApi.path("components").path("schemas").path(schemaName);
    assertThat(schema.has("allOf"))
        .as("%s should be a flat object schema for TypeScript generators", schemaName)
        .isFalse();
    assertThat(schema.path("type").asText())
        .as("%s type", schemaName)
        .isEqualTo("object");
  }

  private static void assertNoSchemaComposesDiscriminatedUnionThroughAllOf(JsonNode openApi) {
    final var schemas = openApi.path("components").path("schemas");
    final var schemaProperties = schemas.properties();
    final var discriminatedUnionSchemaNames = schemaProperties.stream()
        .filter(entry -> entry.getValue().path("oneOf").isArray())
        .map(Map.Entry::getKey)
        .toList();

    final var recursiveCompositionRefs = schemaProperties.stream()
        .filter(entry -> entry.getValue().path("allOf").isArray())
        .flatMap(entry -> StreamSupport.stream(entry.getValue().path("allOf").spliterator(), false)
            .map(part -> part.path("$ref").asText())
            .filter(ref -> !ref.isBlank())
            .map(ref -> entry.getKey() + " -> " + ref.substring(ref.lastIndexOf('/') + 1)))
        .filter(ref -> discriminatedUnionSchemaNames.stream().anyMatch(ref::endsWith))
        .toList();

    assertThat(recursiveCompositionRefs)
        .as("schemas must not compose discriminated oneOf unions through allOf")
        .isEmpty();
  }

  private static void assertStringFormat(
      JsonNode property,
      String format
  ) {
    assertThat(hasType(property, "string")).isTrue();
    assertThat(property.path("format").asText()).isEqualTo(format);
  }

  private static boolean hasType(
      JsonNode property,
      String type
  ) {
    if (type.equals(property.path("type").asText())) {
      return true;
    }

    return StreamSupport.stream(property.path("type").spliterator(), false)
        .map(JsonNode::asText)
        .anyMatch(type::equals);
  }

  private static void assertArray(JsonNode property) {
    assertThat(property.path("type").asText()).isEqualTo("array");
    assertThat(property.path("items").isMissingNode()).isFalse();
  }

  private static void assertEnumProperty(
      JsonNode openApi,
      String schemaName,
      String propertyName,
      String expectedValue
  ) {
    final var schema = resolveSchema(openApi, schemaProperty(openApi, schemaName, propertyName));

    assertThat(schema.path("type").asText()).isEqualTo("string");
    assertThat(StreamSupport.stream(schema.path("enum").spliterator(), false)
        .map(JsonNode::asText)
        .toList())
        .contains(expectedValue);
  }

  private static void assertNoLegacyPhysicalQuantityResponseFields(@NonNull JsonNode openApi) {
    final var legacyFields = Set.of(
        "originalGravityUnit",
        "predictedFinalGravityUnit",
        "sugarUnit",
        "acidityUnit",
        "tanninUnit",
        "yeastAssimilableNitrogenUnit",
        "totalVolumeLiters",
        "totalVolumeUnit",
        "requestedVolumeLiters",
        "requestedVolumeUnit",
        "availableVolumeLiters",
        "availableVolumeUnit",
        "volumeLiters",
        "volumeUnit"
    );

    final var violations = openApi.path("components").path("schemas").properties().stream()
        .filter(entry -> entry.getKey().endsWith("Response"))
        .flatMap(entry -> entry.getValue().path("properties").properties().stream()
            .filter(property -> legacyFields.contains(property.getKey()))
            .map(property -> entry.getKey() + "." + property.getKey()))
        .sorted()
        .toList();

    assertThat(violations)
        .as("response schemas must not expose legacy physical quantity fields")
        .isEmpty();
  }

  private static void assertNoBareNumericResponsePropertiesOutsideAllowList(@NonNull JsonNode openApi) {
    final var allowedBareNumericProperties = Set.of(
        "DecimalQuantityResponse.value",
        "CompositionOptimizationObjectiveAssessmentResponse.score",
        "CompositionOptimizationPenaltyResponse.value",
        "ProfileTargetDistanceResponse.value",
        "ChemicalProfileCompositionSourceResponse.proportion",
        "FeedstockBatchCompositionOptimizationSourceResponse.proportion",
        "ProductionBatchCompositionResponse.targetDistance",
        "ProductionBatchDetailsResponse.targetDistance"
    );

    final var violations = openApi.path("components").path("schemas").properties().stream()
        .filter(entry -> entry.getKey().endsWith("Response"))
        .flatMap(entry -> entry.getValue().path("properties").properties().stream()
            .filter(property -> hasType(property.getValue(), "number"))
            .map(property -> entry.getKey() + "." + property.getKey()))
        .filter(propertyName -> !allowedBareNumericProperties.contains(propertyName))
        .sorted()
        .toList();

    assertThat(violations)
        .as("response schemas must wrap physical numeric values into DecimalQuantityResponse")
        .isEmpty();
  }

  private static JsonNode resolveSchema(
      JsonNode openApi,
      JsonNode schema
  ) {
    final var ref = schema.path("$ref").asText();
    if (ref.isBlank()) {
      return schema;
    }

    final var schemaName = ref.substring(ref.lastIndexOf('/') + 1);
    final var resolved = openApi.path("components").path("schemas").path(schemaName);

    assertThat(resolved.isMissingNode())
        .as("%s schema exists", schemaName)
        .isFalse();
    return resolved;
  }
}
