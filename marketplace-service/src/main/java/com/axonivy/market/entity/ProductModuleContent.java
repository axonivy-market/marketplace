package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import com.axonivy.market.model.MultilingualismValue;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductModuleContent implements Serializable {
	private static final long serialVersionUID = 1L;
	private String tag;
	private MultilingualismValue description;
	private String setup;
	private String demo;
	private Boolean isDependency;
	private String name;
	private String groupId;
	private String artifactId;
	private String type;
}
