package com.axonivy.market.entity;

import com.axonivy.market.enums.FeedbackStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.FEEDBACK;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = FEEDBACK)
public class Feedback extends AuditableIdEntity {

  @Serial
  private static final long serialVersionUID = 1;

  private String userId;
  private String productId;
  @Transient
  @JsonProperty
  private Map<String, String> productNames;
  private String content;
  private Integer rating;
  @Enumerated(EnumType.STRING)
  private FeedbackStatus feedbackStatus;
  private String moderatorName;
  private LocalDateTime reviewDate;
  @Version
  private Integer version;
  private Boolean isLatest;

  public void setContent(String content) {
    if(content != null) {
      this.content = content.trim();
    } else {
      this.content = null;
    }
  }
}
