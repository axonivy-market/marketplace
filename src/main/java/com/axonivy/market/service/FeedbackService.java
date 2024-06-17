package com.axonivy.market.service;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.User;

import java.util.List;

public interface FeedbackService {
  List<Feedback> getAllFeedbacks();
  Feedback getFeedback(String id);
  Feedback createFeedback(Feedback feedback);
}
