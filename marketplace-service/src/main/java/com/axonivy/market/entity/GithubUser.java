package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.axonivy.market.constants.EntityConstants.GITHUB_USER;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = GITHUB_USER)
public class GithubUser extends GenericIdEntity {

  private String gitHubId;
  private String provider;
  private String username;
  private String name;
  private String avatarUrl;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getId()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(getId(), ((GithubUser) obj).getId()).isEquals();
  }
}
