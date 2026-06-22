package com.axonivy.market.repository;

import com.axonivy.market.entity.AppSetting;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {

  List<AppSetting> findByKeyContainingIgnoreCase(String keyword);

  List<AppSetting> findByCategoryIgnoreCase(String category);

  Optional<AppSetting> findByKey(String keyword);

  @Query("select setting.key from AppSetting setting")
  Set<String> findAllKeys();

  @Transactional
  void deleteByKeyNotIn(Set<String> keys);
}