package com.axonivy.market.entity;

import com.axonivy.market.enums.FeedbackStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.FEEDBACK;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(FEEDBACK)
public class Feedback implements Serializable {

  @Serial
  private static final long serialVersionUID = 29519800556564714L;

  @Id
  private String id;
  private String userId;
  private String productId;
  private String content;
  private Integer rating;
  private FeedbackStatus feedbackStatus;
  private String moderatorName;
  private Date reviewDate;
  @Version
  private int version;
  @CreatedDate
  private Date createdAt;

  @LastModifiedDate
  private Date updatedAt;

  public void setContent(String content) {
    this.content = content != null ? content.trim() : null;
  }
}
