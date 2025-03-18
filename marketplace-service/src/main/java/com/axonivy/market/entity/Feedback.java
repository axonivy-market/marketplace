package com.axonivy.market.entity;

import com.axonivy.market.enums.FeedbackStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.FEEDBACK;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = FEEDBACK)
public class Feedback extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 29519800556564714L;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;
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
