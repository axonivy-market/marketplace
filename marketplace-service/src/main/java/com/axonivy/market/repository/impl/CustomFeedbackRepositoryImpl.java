package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.repository.CustomFeedbackRepository;
import com.axonivy.market.repository.CustomRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.axonivy.market.constants.MongoDBConstants.*;

@AllArgsConstructor
@Repository
public class CustomFeedbackRepositoryImpl extends CustomRepository implements CustomFeedbackRepository {
  private final MongoTemplate mongoTemplate;

  @Override
  public Page<Feedback> searchByProductId(String productId, Pageable pageable) {
    Query query = new Query().with(pageable);
    query.addCriteria(Criteria.where(PRODUCT_ID).is(productId)
        .and(FEEDBACK_STATUS).nin(FeedbackStatus.REJECTED, FeedbackStatus.PENDING));

    List<Feedback> feedbacks = mongoTemplate.find(query, Feedback.class, EntityConstants.FEEDBACK);
    long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Feedback.class);

    return new PageImpl<>(feedbacks, pageable, count);
  }

  @Override
  public Feedback findByUserIdAndProductId(String userId, String productId) {
    Query query = new Query();
    query.addCriteria(Criteria.where(PRODUCT_ID).is(productId)
        .andOperator(Criteria.where(USER_ID).is(userId),
            Criteria.where(FEEDBACK_STATUS).ne(FeedbackStatus.REJECTED)));

    List<Feedback> feedbacks = mongoTemplate.find(query, Feedback.class, EntityConstants.FEEDBACK);
    return CollectionUtils.isEmpty(feedbacks) ? null : feedbacks.get(0);
  }

  @Override
  public List<Feedback> findFeedbackByUser(String userId, String productId) {
    Query query = new Query();
    query.addCriteria(Criteria.where(PRODUCT_ID).is(productId)
        .andOperator(Criteria.where(USER_ID).is(userId),
            Criteria.where(FEEDBACK_STATUS).ne(FeedbackStatus.REJECTED)));

    return mongoTemplate.find(query, Feedback.class, EntityConstants.FEEDBACK);
  }
}
