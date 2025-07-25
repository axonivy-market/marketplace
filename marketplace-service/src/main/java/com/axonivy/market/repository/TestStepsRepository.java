package com.axonivy.market.repository;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestStepsRepository extends JpaRepository<TestStep, String> {
    @Query("SELECT ts FROM GithubRepo repo JOIN repo.testSteps ts\n" +
            "WHERE repo.name = :repoName AND ts.type = :workflowType")
    List<TestStep> findByRepoAndWorkflowAndType(
            @Param("repoName") String repoName,
            @Param("workflowType") WorkFlowType workflowType
    );
}
