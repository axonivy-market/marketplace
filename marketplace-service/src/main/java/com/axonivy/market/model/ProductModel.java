package com.axonivy.market.model;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Relation(collectionRelation = "products", itemRelation = "product")
@JsonInclude(Include.NON_NULL)
public class ProductModel extends RepresentationModel<ProductModel> {
	private String id;
	private MultilingualismValue names;
	private MultilingualismValue shortDescriptions;
	private String logoUrl;
	private String type;
	private List<String> tags;

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		return new EqualsBuilder().append(id, ((ProductModel) obj).getId()).isEquals();
	}
}
