package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

import static com.axonivy.market.constants.EntityConstants.GITHUB_USER;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = GITHUB_USER)
public class User implements Serializable {
  @Serial
  private static final long serialVersionUID = -1244486023332931059L;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;
  private String gitHubId;
  private String provider;
  private String username;
  private String name;
  private String avatarUrl;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(id, ((User) obj).getId()).isEquals();
  }
}
