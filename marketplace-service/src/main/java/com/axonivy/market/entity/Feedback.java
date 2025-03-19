package com.axonivy.market.entity;

import com.axonivy.market.enums.FeedbackStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.FEEDBACK;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = FEEDBACK)
public class Feedback extends AuditableIdEntity {

  private String userId;
  private String productId;
  private String content;
  private Integer rating;
  @Enumerated(EnumType.STRING)
  private FeedbackStatus feedbackStatus;
  private String moderatorName;
  private Date reviewDate;
  @Version
  private Integer version;

  public void setContent(String content) {
    this.content = content != null ? content.trim() : null;
  }
}
