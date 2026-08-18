package com.axonivy.market.controller;

import com.axonivy.market.logging.LogStreamRegistry;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.service.LogService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(LogController.class)
class LogControllerTest extends WebMvcControllerTestSupport {

  private static final String TASK_KEY = "syncProducts";

  @MockitoBean
  private LogService logService;

  @Test
  void testListGzLogsWithDate() throws Exception {
    List<LogFileModel> mockLogs = List.of(
        new LogFileModel("application.2026-02-26.log.gz", 2048L, "2026-02-26"));
    when(logService.listGzLogNamesByDate("2026-02-26")).thenReturn(mockLogs);

    mockMvc.perform(get("/api/logs").param("date", "2026-02-26"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].fileName").value("application.2026-02-26.log.gz"));
  }

  @Test
  void testDownloadLogFileExists() throws Exception {
    String fileName = "application.log";
    when(logService.isLogFileExisted(fileName)).thenReturn(true);

    mockMvc.perform(get("/api/logs/download").param("fileName", fileName))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", containsString("attachment")))
        .andExpect(header().string("Content-Disposition", containsString(fileName)))
        .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE));
  }

  @Test
  void testStreamLogsByTaskKey() throws Exception {
    Flux<String> flux = Flux.just("Log 1", "Log 2", "Log 3");

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(() -> LogStreamRegistry.asFlux(TASK_KEY)).thenReturn(flux);

      MvcResult result = mockMvc.perform(get("/api/logs/stream/{taskKey}", TASK_KEY)
              .accept(MediaType.TEXT_EVENT_STREAM))
          .andExpect(request().asyncStarted())
          .andReturn();

      mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(result))
          .andExpect(status().isOk())
          .andDo(print())
          .andExpect(header().string("Content-Type", containsString(MediaType.TEXT_EVENT_STREAM_VALUE)));
    }
  }
}
