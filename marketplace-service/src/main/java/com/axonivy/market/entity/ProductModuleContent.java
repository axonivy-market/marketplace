package com.axonivy.market.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = PRODUCT_MODULE_CONTENT)
@EntityListeners(AuditingEntityListener.class)
public class ProductModuleContent implements Serializable {
  @Id
  private String id;
  @Serial
  private static final long serialVersionUID = 1L;
  @Schema(description = "product Id (from meta.json)", example = "portal")
  private String productId;
  @Schema(description = "Maven version", example = "10.0.25")
  private String version;

  @Schema(description = "Product detail description content ",
      example = "{ \"de\": \"E-Sign-Konnektor\", \"en\": \"E-sign connector\" }")
  @ElementCollection
  @CollectionTable(name = PRODUCT_MODULE_CONTENT_DESCRIPTION,
      joinColumns = @JoinColumn(name = PRODUCT_MODULE_CONTENT_ID))
  @MapKeyColumn(name = LANGUAGE)
  @Column(name = DESCRIPTION, columnDefinition = TEXT_TYPE)
  private Map<String, String> description;

  @Schema(description = "Setup tab content", example = "{ \"de\": \"Setup\", \"en\": \"Setup\" ")
  @ElementCollection
  @CollectionTable(name = PRODUCT_MODULE_CONTENT_SETUP, joinColumns = @JoinColumn(name =
      PRODUCT_MODULE_CONTENT_ID))
  @MapKeyColumn(name = LANGUAGE)
  @Column(name = SETUP, columnDefinition = TEXT_TYPE)
  private Map<String, String> setup;

  @Schema(description = "Demo tab content", example = "{ \"de\": \"Demo\", \"en\": \"Demo\" ")
  @ElementCollection
  @CollectionTable(name = PRODUCT_MODULE_CONTENT_DEMO, joinColumns = @JoinColumn(name =
      PRODUCT_MODULE_CONTENT_ID))
  @MapKeyColumn(name = LANGUAGE)
  @Column(name = DEMO, columnDefinition = TEXT_TYPE)
  private Map<String, String> demo;

  @Schema(description = "Is dependency artifact", example = "true")
  private Boolean isDependency;
  @Schema(example = "Adobe Acrobat Sign Connector")
  private String name;
  @Schema(description = "Product artifact's group id", example = "com.axonivy.connector.adobe.acrobat.sign")
  private String groupId;
  @Schema(description = "Product artifact's artifact id", example = "adobe-acrobat-sign-connector-product")
  private String artifactId;
  @Schema(description = "Artifact file type", example = "iar")
  private String type;
  @LastModifiedDate
  private Date updatedAt;
}
