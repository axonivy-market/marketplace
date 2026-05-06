package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Preview constants defining preview directory paths and image download URL patterns for image preview functionality.
 * </p>
 *
 * @since 15/04/2026
 * @author pvquan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PreviewConstants {

  public static final String PREVIEW_DIR = "data/work/preview";

  public static final String IMAGE_DOWNLOAD_URL = "%s/api/image/preview/%s";

}
