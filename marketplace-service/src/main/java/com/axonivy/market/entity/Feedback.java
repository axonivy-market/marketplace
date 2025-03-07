package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import static com.axonivy.market.constants.EntityConstants.FEEDBACK;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = FEEDBACK)
@EntityListeners(AuditingEntityListener.class)
public class Feedback implements Serializable {

  @Serial
  private static final long serialVersionUID = 29519800556564714L;

  @Id
  private String id;
  private String userId;
  private String productId;
  private String content;
  private Integer rating;

  @CreatedDate
  private Date createdAt;

  @LastModifiedDate
  private Date updatedAt;

  public void setContent(String content) {
    this.content = content != null ? content.trim() : null;
  }

  @PrePersist
  private void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }
}
