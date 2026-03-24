package com.axonivy.market.entity;

import com.axonivy.market.core.entity.GenericIdEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.axonivy.market.constants.EntityConstants.GITHUB_USER;

import java.io.Serial;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = GITHUB_USER)
public class GithubUser extends GenericIdEntity {

  @Serial
  private static final long serialVersionUID = 1;

  private String gitHubId;
  private String provider;
  private String username;
  private String name;
  private String avatarUrl;
  private String url;

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof GithubUser other)) return false;

    return getId() != null && getId().equals(other.getId());
  }
}
