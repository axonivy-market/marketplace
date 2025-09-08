package com.axonivy.market.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serial;

@MappedSuperclass
public class AuditableIdEntity extends AbstractAuditableEntity<String> {

  @Serial
  private static final long serialVersionUID = 1;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }
}
