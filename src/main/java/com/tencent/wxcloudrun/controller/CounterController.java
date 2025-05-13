package com.tencent.wxcloudrun.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.CounterRequest;
import com.tencent.wxcloudrun.model.Counter;
import com.tencent.wxcloudrun.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * counter控制器
 */
@RestController

public class CounterController {

  final CounterService counterService;
  final Logger logger;
  public static String fetchTriviaQuestions(String apiUrl) throws IOException {
    StringBuilder response = new StringBuilder();
    HttpURLConnection connection = null;
    BufferedReader reader = null;

    try {
      // 创建URL对象并打开连接
      URL url = new URL(apiUrl);
      connection = (HttpURLConnection) url.openConnection();

      // 设置请求方法
      connection.setRequestMethod("GET");

      // 设置请求头
      connection.setRequestProperty("Accept", "application/json");

      // 获取响应码
      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // 读取响应内容
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
      } else {
        throw new IOException("HTTP error code: " + responseCode);
      }

    } finally {
      // 关闭资源
      if (reader != null) {
        reader.close();
      }
      if (connection != null) {
        connection.disconnect();
      }
    }

    return response.toString();
  }
  public CounterController(@Autowired CounterService counterService) {
    this.counterService = counterService;
    this.logger = LoggerFactory.getLogger(CounterController.class);
  }


  /**
   * 获取当前计数
   * @return API response json
   */
  @GetMapping(value = "/api/count")
  ApiResponse get() {
//    logger.info("/api/count get request");
//    Optional<Counter> counter = counterService.getCounter(1);
//    Integer count = 0;
//    if (counter.isPresent()) {
//      count = counter.get().getCount()+3;
//    }
    String apiUrl = "https://opentdb.com/api.php?amount=10&category=18&difficulty=easy&type=multiple";
    String jsonResponse = "";
    try {
      jsonResponse = fetchTriviaQuestions(apiUrl);
      System.out.println("API Response:");
      System.out.println(jsonResponse);
    } catch (IOException e) {
      System.err.println("Error fetching trivia questions: " + e.getMessage());
    }
    return ApiResponse.ok(jsonResponse);
  }


  /**
   * 更新计数，自增或者清零
   * @param request {@link CounterRequest}
   * @return API response json
   */
  @PostMapping(value = "/api/count")
  ApiResponse create(@RequestBody CounterRequest request) {
    logger.info("/api/count post request, action: {}", request.getAction());

    Optional<Counter> curCounter = counterService.getCounter(1);
    if (request.getAction().equals("inc")) {
      Integer count = 1;
      if (curCounter.isPresent()) {
        count += curCounter.get().getCount();
      }
      Counter counter = new Counter();
      counter.setId(1);
      counter.setCount(count);
      counterService.upsertCount(counter);
      return ApiResponse.ok(count);
    } else if (request.getAction().equals("clear")) {
      if (!curCounter.isPresent()) {
        return ApiResponse.ok(0);
      }
      counterService.clearCount(1);
      return ApiResponse.ok(0);
    } else {
      return ApiResponse.error("参数action错误");
    }
  }
  
}
