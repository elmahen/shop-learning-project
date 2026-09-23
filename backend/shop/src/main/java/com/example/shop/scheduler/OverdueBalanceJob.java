package com.example.shop.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.shop.service.CustomerService;

@Component
public class OverdueBalanceJob {

    private final CustomerService customerService;

    public OverdueBalanceJob(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void checkOverdueBalances() {
        customerService.notifyOverdueCustomers();
    }
}