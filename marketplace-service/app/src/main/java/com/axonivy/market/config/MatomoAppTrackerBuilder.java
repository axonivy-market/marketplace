package com.axonivy.market.config;

import com.axonivy.market.core.config.MatomoTrackerBuilder;
import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.service.AppSettingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * <p>
 * MatomoAppTrackerBuilder
 * </p> 
 *
 * @since 08/07/2026
 * @author thxhuy
 */
@Log4j2
@Component
public class MatomoAppTrackerBuilder extends MatomoTrackerBuilder {
  public MatomoAppTrackerBuilder(AppSettingService appSettingService) {
    super(appSettingService);
  }

  @Override
  protected AppSettingKey getEndpointKey() {
    return AppSettingKey.MATOMO_API_ENDPOINT;
  }

  @Override
  protected AppSettingKey getSiteIdKey() {
    return AppSettingKey.MATOMO_SITE_ID;
  }

  @Override
  protected AppSettingKey getEnabledKey() {
    return AppSettingKey.MATOMO_ENABLED;
  }
}
