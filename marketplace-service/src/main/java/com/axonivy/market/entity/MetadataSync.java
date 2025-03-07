package com.axonivy.market.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.Set;

import static com.axonivy.market.constants.EntityConstants.MAVEN_METADATA_SYNC;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = MAVEN_METADATA_SYNC)
@EntityListeners(AuditingEntityListener.class)
public class MetadataSync {
  @Id
  private String productId;

  @ElementCollection
  @CollectionTable(name = "metadata_sync_versions", joinColumns = @JoinColumn(name = "product_id"))
  @Column(name = "synced_version")
  private Set<String> syncedVersions;

  @LastModifiedDate
  private Date lastSync;
}
