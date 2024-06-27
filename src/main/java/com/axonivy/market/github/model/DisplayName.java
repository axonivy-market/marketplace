package com.axonivy.market.github.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayName {

	private String locale;
	private String value;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DisplayName)) {
			return false;
		}
		DisplayName other = (DisplayName) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(value, other.getValue());
		builder.append(locale, other.locale);
		return builder.isEquals();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(getValue());
		builder.append(getLocale());
		return builder.hashCode();
	}
}
