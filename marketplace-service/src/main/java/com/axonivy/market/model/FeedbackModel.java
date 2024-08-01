package com.axonivy.market.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Relation(collectionRelation = "feedbacks", itemRelation = "feedback")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedbackModel extends RepresentationModel<FeedbackModel> {
  @Schema(description = "Id of feedback", example = "667940ecc881b1d0db072f9e")
  private String id;

  @Schema(description = "User Id", example = "666ff14c847c664ac54d2643")
  private String userId;

  @Schema(description = "Github username", example = "ntqdinh-axonivy")
  private String username;

  @Schema(description = "Url of github avatar", example = "https://avatars.githubusercontent.com/u/1?v=4")
  private String userAvatarUrl;

  @Schema(description = "3rd party login provider", example = "GitHub")
  private String userProvider;

  @Schema(description = "Product id (from meta.json)", example = "portal")
  private String productId;

  @Schema(description = "User's feedback content", example = "Pretty cool connector.")
  private String content;

  @Schema(description = "User's rating point of target product", example = "5", minimum = "1", maximum = "5")
  private Integer rating;

  @Schema(description = "Feedback/rating creating timestamp", example = "2024-06-24T00:00:00.000Z")
  private Date createdAt;

  @Schema(description = "Latest feedback/rating updating timestamp", example = "2024-06-24T00:00:00.000Z")
  private Date updatedAt;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(id, ((FeedbackModel) obj).getId()).isEquals();
  }
}
