package com.axonivy.market.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiTaskUtilsTest {

  @Test
  void parallelProcessWithLimitUsesInjectedExecutor() {
    Executor directExecutor = Runnable::run;
    MultiTaskUtils multiTaskUtils = new MultiTaskUtils(directExecutor);

    List<Integer> result = multiTaskUtils.parallelProcessWithLimit(List.of(1, 2, 3), value -> value * 2, 2);

    assertEquals(List.of(2, 4, 6), result);
  }
}
