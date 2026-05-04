//package com.axonivy.market.aspect;
//
//import com.axonivy.market.aop.annotation.TimeoutGuarded;
//import com.axonivy.market.aop.aspect.TimeoutGuardAspect;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.core.env.Environment;
//
//import java.net.SocketTimeoutException;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class TimeoutGuardAspectTest {
//
//  private TimeoutGuardAspect aspect;
//  private ProceedingJoinPoint pjp;
//  private TimeoutGuarded timeoutGuarded;
//  private Environment environment;
//
//  @BeforeEach
//  void setUp() {
//    environment = mock(Environment.class);
//    aspect = new TimeoutGuardAspect(environment);
//    pjp = mock(ProceedingJoinPoint.class);
//    timeoutGuarded = mock(TimeoutGuarded.class);
//
//    Signature signature = mock(Signature.class);
//    when(signature.toShortString()).thenReturn("TestService.doWork()");
//    when(pjp.getSignature()).thenReturn(signature);
//  }
//
//  @Test
//  void testAroundTimeoutSuccess() throws Throwable {
//    when(timeoutGuarded.timeoutSeconds()).thenReturn("${timeout:10}");
//    when(environment.resolvePlaceholders("${timeout:10}")).thenReturn("10");
//    when(pjp.proceed()).thenReturn("result");
//
//    Object result = aspect.aroundTimeout(pjp, timeoutGuarded);
//
//    assertEquals("result", result, "Should return the value from pjp.proceed()");
//  }
//
//  @Test
//  void testAroundTimeoutExceedsLimit() throws Throwable {
//    when(timeoutGuarded.timeoutSeconds()).thenReturn("${timeout:1}");
//    when(environment.resolvePlaceholders("${timeout:1}")).thenReturn("1");
//    when(pjp.proceed()).thenAnswer(invocation -> {
//      Thread.sleep(3000);
//      return "late";
//    });
//
//    assertThrows(SocketTimeoutException.class, () -> aspect.aroundTimeout(pjp, timeoutGuarded),
//        "Should throw SocketTimeoutException when method exceeds timeout");
//  }
//
//  @Test
//  void testAroundTimeoutPropagatesException() throws Throwable {
//    when(timeoutGuarded.timeoutSeconds()).thenReturn("${timeout:10}");
//    when(environment.resolvePlaceholders("${timeout:10}")).thenReturn("10");
//    when(pjp.proceed()).thenThrow(new RuntimeException("fail"));
//
//    RuntimeException thrown = assertThrows(RuntimeException.class,
//        () -> aspect.aroundTimeout(pjp, timeoutGuarded),
//        "Should propagate the original exception");
//    assertEquals("fail", thrown.getMessage());
//  }
//
//  @Test
//  void testAroundTimeoutFallsBackOnInvalidTimeout() throws Throwable {
//    when(timeoutGuarded.timeoutSeconds()).thenReturn("${timeout}");
//    when(environment.resolvePlaceholders("${timeout}")).thenReturn("not-a-number");
//    when(pjp.proceed()).thenReturn("ok");
//
//    Object result = aspect.aroundTimeout(pjp, timeoutGuarded);
//
//    assertEquals("ok", result, "Should fall back to default timeout and still succeed");
//  }
//}
//
