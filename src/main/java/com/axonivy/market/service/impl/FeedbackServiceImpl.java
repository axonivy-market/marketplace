package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

  private final FeedbackRepository feedbackRepository;

  public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
    this.feedbackRepository = feedbackRepository;
  }

  @Override
  public List<Feedback> getAllFeedbacks() {
    return feedbackRepository.findAll();
  }

  @Override
  public Feedback getFeedback(String id) throws NotFoundException {
    return feedbackRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found feedback with id: " + id));
  }

  @Override
  public Feedback createFeedback(Feedback feedback) {
    return feedbackRepository.save(feedback);
  }
}
