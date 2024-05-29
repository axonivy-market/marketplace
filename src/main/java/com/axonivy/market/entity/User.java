package com.axonivy.market.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.axonivy.market.constants.DocumentConstants.USER_DOCUMENT;

@Getter
@Setter
@NoArgsConstructor
@Document(USER_DOCUMENT)
public class User {
    @Id
    private String id;
    private String username;
    private String password;
}
