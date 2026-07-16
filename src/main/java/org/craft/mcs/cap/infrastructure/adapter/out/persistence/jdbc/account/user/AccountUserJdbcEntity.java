package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;

/** Spring Data JDBC aggregate entity for a CAP account user. */
@Table("account_users")
public class AccountUserJdbcEntity {

  @Id @NonNull @Column("user_id") UUID userId;
  @NonNull @Column("identity_provider") String identityProvider;
  @NonNull @Column("external_subject") String externalSubject;
  @NonNull @Column("email") String email;
  @NonNull @Column("display_name") String displayName;
  @Nullable @Column("avatar_url") String avatarUrl;
  @NonNull @Column("status") AccountUserStatus status;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public AccountUserJdbcEntity() {
  }

  AccountUserJdbcEntity(
      @NonNull UUID userId,
      @NonNull String identityProvider,
      @NonNull String externalSubject,
      @NonNull String email,
      @NonNull String displayName,
      @Nullable String avatarUrl,
      @NonNull AccountUserStatus status
  ) {
    this.userId = requireNonNull(userId, "account user id cannot be null");
    this.identityProvider = requireNonNull(identityProvider, "identity provider cannot be null");
    this.externalSubject = requireNonNull(externalSubject, "external subject cannot be null");
    this.email = requireNonNull(email, "account email cannot be null");
    this.displayName = requireNonNull(displayName, "account display name cannot be null");
    this.avatarUrl = avatarUrl;
    this.status = requireNonNull(status, "account user status cannot be null");
  }
}
