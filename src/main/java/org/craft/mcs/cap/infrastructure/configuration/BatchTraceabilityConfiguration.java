package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.InitializeMaterialStateService;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.QueryBatchTraceabilityService;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.QueryMaterialStateService;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.RebuildCurrentMaterialStatesService;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.TransitionMaterialStatesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class BatchTraceabilityConfiguration {

  @Bean
  InitializeMaterialStateService initializeMaterialStateService(
      BatchTraceabilityStore traceabilityStore,
      CurrentMaterialStateStore currentStateStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new InitializeMaterialStateService(traceabilityStore, currentStateStore, accessControl);
  }

  @Bean
  QueryMaterialStateService queryMaterialStateService(
      CurrentMaterialStateStore currentStateStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new QueryMaterialStateService(currentStateStore, accessControl);
  }

  @Bean
  TransitionMaterialStatesService transitionMaterialStatesService(
      BatchTraceabilityStore traceabilityStore,
      CurrentMaterialStateStore currentStateStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new TransitionMaterialStatesService(traceabilityStore, currentStateStore, accessControl);
  }

  @Bean
  RebuildCurrentMaterialStatesService rebuildCurrentMaterialStatesService(
      BatchTraceabilityQueryStore traceabilityQueryStore,
      CurrentMaterialStateStore currentStateStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new RebuildCurrentMaterialStatesService(
        traceabilityQueryStore,
        currentStateStore,
        accessControl
    );
  }

  @Bean
  QueryBatchTraceabilityService queryBatchTraceabilityService(
      BatchTraceabilityQueryStore queryStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new QueryBatchTraceabilityService(queryStore, accessControl);
  }
}
