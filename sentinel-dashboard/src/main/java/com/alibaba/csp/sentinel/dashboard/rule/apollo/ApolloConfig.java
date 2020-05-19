package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApolloConfig {

//    @Value("${apollo.portalUrl}")
//    private String portalUrl;
//    @Value("${apollo.token}")
//    private String token;

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        ApolloOpenApiClient client = ApolloOpenApiClient.newBuilder()
                 .withPortalUrl("http://106.14.83.136:8070")
//                .withPortalUrl()
                 .withToken("126fdbbf7ae2bc9e62a2a266e230f03376f5c755")
//                .withToken()
                .build();
        return client;
    }

}
