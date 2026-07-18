package com.fulfillflow.fulfillment;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class FulfillmentTaskService {
    private final FulfillmentTaskRepository tasks;

    FulfillmentTaskService(FulfillmentTaskRepository tasks) {
        this.tasks = tasks;
    }

    @Transactional
    void createForOrder(UUID orderId) {
        if (!tasks.existsByOrderId(orderId)) {
            tasks.save(new FulfillmentTask(orderId));
        }
    }

    @Transactional
    void completeForOrder(UUID orderId) {
        tasks.findByOrderId(orderId).ifPresent(FulfillmentTask::complete);
    }

    @Transactional
    void cancelForOrder(UUID orderId) {
        tasks.findByOrderId(orderId).ifPresent(FulfillmentTask::cancel);
    }

    @Transactional(readOnly = true)
    List<FulfillmentTaskResponse> list() {
        return tasks.findAllByOrderByCreatedAtAsc().stream().map(FulfillmentTaskResponse::from).toList();
    }
}
