package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.WorkflowRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepoRepository extends JpaRepository<WorkflowRepo,String> {
  List<WorkflowRepo> findByRepository(GithubRepo existingRepo);
}
