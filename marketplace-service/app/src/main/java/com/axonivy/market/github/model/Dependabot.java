package com.axonivy.market.github.model;

import com.axonivy.market.enums.AccessLevel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Dependabot implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Integer> alerts;

  @Enumerated(EnumType.STRING)
  private AccessLevel status;
}
