package com.axonivy.market.repository;

import com.axonivy.market.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {

  List<AppSetting> findByKeyContainingIgnoreCase(String keyword);

  Optional<AppSetting> findByKey(String keyword);

  @Query("select setting.key from AppSetting setting")
  Set<String> findAllKeys();
}