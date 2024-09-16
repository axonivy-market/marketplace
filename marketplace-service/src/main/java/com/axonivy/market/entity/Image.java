package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.axonivy.market.constants.EntityConstants.IMAGE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(IMAGE)
public class Image {
  @Id
  private String id;
  private String productId;
  private String imageUrl;
  private Binary imageData;
  private String sha;
}
