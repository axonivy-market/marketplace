package com.axonivy.market.repository;

import com.axonivy.market.entity.TestStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestStepsRepository extends JpaRepository<TestStep, String> {
  @Query("SELECT ts FROM TestStep ts " +
      "LEFT JOIN ts.repository repo " +
      "WHERE repo.name = :repoName AND ts.type = :workflowType")
  List<TestStep> findByRepoAndWorkflowAndType(
      @Param("repoName") String repoName,
      @Param("workflowType") String workflowType
  );
}
