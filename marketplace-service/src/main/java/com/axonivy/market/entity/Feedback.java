package com.axonivy.market.entity;

import com.axonivy.market.enums.FeedbackStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import jakarta.persistence.*;

import java.util.Date;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.*;
import static com.axonivy.market.constants.ProductJsonConstants.NAME;
import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_ID;

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
  @Transient
  @JsonProperty
//  @CollectionTable(name = PRODUCT_NAME, joinColumns = @JoinColumn(name = PRODUCT_ID))
//  @Column(name = NAME, columnDefinition = TEXT_TYPE)
//  @MapKeyColumn(name = LANGUAGE)
  private Map<String, String> productNames;
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
