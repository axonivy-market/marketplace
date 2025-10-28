package com.axonivy.market.model;

import com.axonivy.market.entity.GenericIdEntity;
import com.axonivy.market.enums.WorkFlowType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.WORKFLOW_INFORMATION;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = WORKFLOW_INFORMATION)
public class WorkflowInformation extends GenericIdEntity {
  @Serial
  private static final long serialVersionUID = 1L;
  @Enumerated(EnumType.STRING)
  private WorkFlowType workflowType;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "UTC")
  private Date lastBuilt;
  private String conclusion;
  private String lastBuiltRunUrl;
  private String currentWorkflowState;
  private Date disabledDate;
}
