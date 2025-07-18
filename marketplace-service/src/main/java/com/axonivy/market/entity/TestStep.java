package com.axonivy.market.entity;

import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "test_step")
public class TestStep extends GenericIdEntity {
    private String name;
    @Enumerated(EnumType.STRING)
    private TestStatus status;
    @Enumerated(EnumType.STRING)
    private WorkFlowType type;
    @Enumerated(EnumType.STRING)
    private TestEnviroment testType;
    @ManyToOne
    private GithubRepo repository;
}
