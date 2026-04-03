package com.axonivy.market.service;

import com.axonivy.market.github.model.DisabledSecurityEvent;

import java.util.List;

public interface NotificationService {

  /**
   * <p>
   * notify mail by Mime message
   * </p>
   *
   * @param  events
   *              type {@link List<DisabledSecurityEvent>}
   * @return {@link }
   * @author nntthuy
   */
  void notify(List<DisabledSecurityEvent> events);
}