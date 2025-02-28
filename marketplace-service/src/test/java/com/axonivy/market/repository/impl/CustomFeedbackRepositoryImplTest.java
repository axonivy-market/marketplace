package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.FeedbackStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomFeedbackRepositoryImplTest extends BaseSetup {
  private static final String PRODUCT_ID = "product1";
  private static final String USER_ID = "user1";

  @Mock
  private MongoTemplate mongoTemplate;
  @InjectMocks
  private CustomFeedbackRepositoryImpl customFeedbackRepository;

  @Test
  void testSearchByProductId_SuccessfulSearch() {
    Pageable pageable = PageRequest.of(0, 10);
    Feedback feedback = Feedback.builder().productId(PRODUCT_ID).userId(USER_ID).feedbackStatus(
        FeedbackStatus.APPROVED).build();

    List<Feedback> feedbackList = List.of(feedback);
    long totalCount = 1L;

    Query expectedQuery = new Query().with(pageable).addCriteria(
        Criteria.where(MongoDBConstants.PRODUCT_ID).is(PRODUCT_ID).and(MongoDBConstants.FEEDBACK_STATUS).nin(
            FeedbackStatus.REJECTED, FeedbackStatus.PENDING));

    when(mongoTemplate.find(expectedQuery, eq(Feedback.class), eq(EntityConstants.FEEDBACK))).thenReturn(
        feedbackList);
    when(mongoTemplate.count(any(Query.class), eq(Feedback.class))).thenReturn(totalCount);

    Page<Feedback> result = customFeedbackRepository.searchByProductId(PRODUCT_ID, pageable);

    assertNotNull(result);
    assertEquals(totalCount, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    assertEquals(feedback, result.getContent().get(0));

    verify(mongoTemplate, times(1)).find(expectedQuery, eq(Feedback.class), eq(EntityConstants.FEEDBACK));
    verify(mongoTemplate, times(1)).count(any(Query.class), eq(Feedback.class));
  }

  @Test
  void testFindByUserIdAndProductId() {
    Feedback feedback = Feedback.builder().productId(PRODUCT_ID).userId(USER_ID).feedbackStatus(
        FeedbackStatus.APPROVED).build();

    List<Feedback> expectedList = List.of(feedback);

    Query expectedQuery = new Query().addCriteria(
        Criteria.where(MongoDBConstants.PRODUCT_ID).is(PRODUCT_ID).andOperator(
            Criteria.where(MongoDBConstants.USER_ID).is(USER_ID),
            Criteria.where(MongoDBConstants.FEEDBACK_STATUS).ne(FeedbackStatus.REJECTED)));

    when(mongoTemplate.find(expectedQuery, eq(Feedback.class), eq(EntityConstants.FEEDBACK))).thenReturn(
        expectedList);

    List<Feedback> result = customFeedbackRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(feedback, result.get(0));

    verify(mongoTemplate, times(1)).find(expectedQuery, eq(Feedback.class), eq(EntityConstants.FEEDBACK));
  }
}