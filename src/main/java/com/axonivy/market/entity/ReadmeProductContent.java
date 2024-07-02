package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadmeProductContent implements Serializable {
	private String tag;
	private String description;
	private String setup;
	private String demo;
	private Boolean isDependency;
	private String name;
	private String groupId;
	private String artifactId;
	private String type;
}
