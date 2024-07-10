package com.axonivy.market.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.FEEDBACK;

@Getter
@Setter
@NoArgsConstructor
@Document(FEEDBACK)
public class Feedback implements Serializable {

  @Serial
  private static final long serialVersionUID = 29519800556564714L;

  @Id
  private String id;

  private String userId;

  @NotBlank(message = "Product id cannot be blank")
  private String productId;

  @NotBlank(message = "Content cannot be blank")
  @Size(max = 5, message = "Content length must be up to 250 characters")
  private String content;

  @Min(value = 1, message = "Rating should not be less than 1")
  @Max(value = 5, message = "Rating should not be greater than 5")
  private Integer rating;

  @CreatedDate
  private Date createdAt;

  @LastModifiedDate
  private Date updatedAt;

  public void setContent(String content) {
    this.content = content != null ? content.trim() : null;
  }
}
