package com.axonivy.market.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String id;
    private String userId;
    private String content;
    private Integer rating;
    private Date createdAt;
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
