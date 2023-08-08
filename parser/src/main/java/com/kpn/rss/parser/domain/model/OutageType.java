package com.kpn.rss.parser.domain.model;

public enum OutageType {
    BUSINESS,
    CUSTOMER;

    public boolean isBusinessOutage() {
        return this == BUSINESS;
    }

    public boolean isCustomerOutage() {
        return this == CUSTOMER;
    }
}
