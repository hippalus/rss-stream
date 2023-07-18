package com.kpn.rss.parser.domain.service;

import com.kpn.rss.parser.domain.model.Outage;

import java.util.Set;

public interface OutageService {

    void saveCustomerOutages(final Set<Outage> outages);

    void saveBusinessOutages(final Set<Outage> outages);

}
