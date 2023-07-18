package com.kpn.rss.fetcher;

import com.kpn.rss.fetcher.common.IntegrationTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
class FetcherApplicationTests {

    @Test
    void contextLoads() {
    }

}
