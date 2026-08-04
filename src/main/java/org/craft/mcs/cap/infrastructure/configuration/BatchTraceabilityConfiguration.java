package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateHistoryStore;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateLineageStore;
import org.craft.mcs.cap.core.traceability.application.usecase.AppendBatchTraceStepService;
import org.craft.mcs.cap.core.traceability.application.usecase.GetMaterialStateService;
import org.craft.mcs.cap.core.traceability.application.usecase.QueryBatchTraceabilityService;
import org.craft.mcs.cap.core.traceability.application.usecase.QueryMaterialStateLineageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BatchTraceabilityConfiguration {

  @Bean
  AppendBatchTraceStepService appendBatchTraceStepService(
      BatchTraceabilityStore traceabilityStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new AppendBatchTraceStepService(traceabilityStore, accessControl);
  }

  @Bean
  GetMaterialStateService getMaterialStateService(
      MaterialStateHistoryStore historyStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new GetMaterialStateService(historyStore, accessControl);
  }

  @Bean
  QueryBatchTraceabilityService queryBatchTraceabilityService(
      BatchTraceabilityQueryStore queryStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new QueryBatchTraceabilityService(queryStore, accessControl);
  }

  @Bean
  QueryMaterialStateLineageService queryMaterialStateLineageService(
      MaterialStateLineageStore lineageStore,
      BatchTraceabilityAccessControl accessControl
  ) {
    return new QueryMaterialStateLineageService(lineageStore, accessControl);
  }
}
