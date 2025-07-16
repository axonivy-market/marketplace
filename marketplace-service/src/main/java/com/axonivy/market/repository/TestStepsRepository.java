package com.axonivy.market.repository;

import com.axonivy.market.entity.TestSteps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TestStepsRepository extends JpaRepository<TestSteps, String> {
  @Query("SELECT ts FROM TestSteps ts " +
      "LEFT JOIN ts.workflow wf " +
      "LEFT JOIN wf.repository repo " +
      "WHERE repo.name = :repoName AND wf.type = :workflowType")
  List<TestSteps> findByRepoAndWorkflowAndType(
      @Param("repoName") String repoName,
      @Param("workflowType") String workflowType
  );
  List<TestSteps> findByWorkflowId(String workflowId);
}
