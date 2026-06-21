package org.example.projectrealizationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "prompt-vector-service", path = "/api/vector")
public interface MilvusClient {

    @PostMapping("/template")
    String createTemplate(@RequestParam("text") String text, 
                          @RequestParam("category") String category);

    @PostMapping("/feedback")
    String createFeedback(@RequestParam("comment") String comment, 
                          @RequestParam("field") String field, 
                          @RequestParam("rating") Integer rating);
}