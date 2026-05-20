package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.AppendBatchTraceStepService;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.QueryBatchTraceabilityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class BatchTraceabilityConfiguration {

  @Bean
  AppendBatchTraceStepService appendBatchTraceStepService(BatchTraceabilityStore store) {
    return new AppendBatchTraceStepService(store);
  }

  @Bean
  QueryBatchTraceabilityService queryBatchTraceabilityService(BatchTraceabilityQueryStore queryStore) {
    return new QueryBatchTraceabilityService(queryStore);
  }
}
