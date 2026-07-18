package com.fulfillflow.fulfillment;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fulfillment-tasks")
class FulfillmentTaskController {
    private final FulfillmentTaskService tasks;

    FulfillmentTaskController(FulfillmentTaskService tasks) { this.tasks = tasks; }

    @GetMapping
    List<FulfillmentTaskResponse> list() { return tasks.list(); }
}
