package com.axonivy.market.entity;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Transient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MavenArtifactModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String downloadUrl;
	@Transient
	private Boolean isProductArtifact;

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		MavenArtifactModel reference = (MavenArtifactModel) object;
		return Objects.equals(name, reference.getName()) && Objects.equals(downloadUrl, reference.getDownloadUrl());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, downloadUrl);
	}
}