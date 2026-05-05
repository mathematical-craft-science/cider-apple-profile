package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockSampleCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockSampleUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSampleId;
import org.craft.mcs.cap.shared.application.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class CreateFeedstockSampleService
    implements CreateFeedstockSampleUseCase {

  private final @NonNull FeedstockSampleRepository feedstockSampleRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull FeedstockSample create(@NonNull CreateFeedstockSampleCommand command) {
    requireNonNull(command, "create feedstock sample command cannot be null");

    final var sample = FeedstockSample.create(
        FeedstockSampleId.newId(),
        command.ciderMakerId(),
        command.name(),
        command.type(),
        command.chemicalProfile(),
        command.collectedAt()
    ).changeNote(command.note());

    return transactionHelper.callInTransactionNonNull(
        () -> feedstockSampleRepository.save(sample)
    );
  }
}
