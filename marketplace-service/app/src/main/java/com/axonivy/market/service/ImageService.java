package com.axonivy.market.service;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.service.CoreImageService;
import org.kohsuke.github.GHContent;

import java.nio.file.Path;

public interface ImageService extends CoreImageService {

  /**
   * <p>
   * Get image binary by GitHub content and download url
   * </p>
   *
   * @param  ghContent
   *              type {@link GHContent}
   * @param  downloadUrl
   *              type {@link String}
   * @return {@link byte[]}
   * @author tvtphuc
   */
  byte[] getImageBinary(GHContent ghContent, String downloadUrl);

  /**
   * <p>
   * Mapping image from GitHub content and product id
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  ghContent
   *              type {@link GHContent}
   * @return {@link Image}
   * @author nntthuy
   */
  Image mappingImageFromGHContent(String productId, GHContent ghContent);

  /**
   * <p>
   * Mapping image from download folder
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  imagePath
   *              type {@link Path}
   * @return {@link Image}
   * @author ndkhanh
   */
  Image mappingImageFromDownloadedFolder(String productId, Path imagePath);

  /**
   * <p>
   * Read image by id
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @return {@link byte[]}
   * @author tvtphuc
   */
  byte[] readImage(String id);

  /**
   * <p>
   * Read preview image by image name
   * </p>
   *
   * @param  imageName
   *              type {@link String}
   * @return {@link byte[]}
   * @author pvquan
   */
  byte[] readPreviewImageByName(String imageName);
}
