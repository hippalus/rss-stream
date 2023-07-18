package com.kpn.rss.parser.infra.streams;

public final class Constants {
    private Constants() {
        throw new AssertionError();
    }

    public static final String CUSTOMER_OUTAGES_STORE = "customer-outages-store";
    public static final String BUSINESS_OUTAGES_STORE = "business-outages-store";

    public static final String BRANCH_PREFIX = "Branch-";
    public static final String BUSINESS = "Business";
    public static final String CUSTOMER = "Customer";
    public static final String BRANCH_BUSINESS = BRANCH_PREFIX + BUSINESS;
    public static final String BRANCH_CUSTOMER = BRANCH_PREFIX + CUSTOMER;
}
