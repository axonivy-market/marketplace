package com.axonivy.market.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

@Getter
@Setter
@Document
public class GitHubRepoMeta {
    private String repoName;
    private Timestamp lastChange;
}
