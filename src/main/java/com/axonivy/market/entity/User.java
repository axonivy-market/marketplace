package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.USER;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Document(USER)
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = -1244486023332931059L;

    @Id
    private String id;

    @Indexed(unique = true)
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
