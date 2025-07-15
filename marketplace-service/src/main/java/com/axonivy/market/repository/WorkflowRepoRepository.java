package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.WorkflowRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepoRepository extends JpaRepository<WorkflowRepo,String> {
  List<WorkflowRepo> findByRepository(GithubRepo repo);
}
