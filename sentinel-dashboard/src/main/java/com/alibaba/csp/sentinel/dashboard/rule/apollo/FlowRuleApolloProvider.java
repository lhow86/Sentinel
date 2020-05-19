package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("flowRuleApolloProvider")
public class FlowRuleApolloProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {

    @Value("${apollo.env:FAT}")
    private String env;

    @Value("${apollo.flowDataId:sentinel.flowRules}")
    private String flowDataId;

    @Value("${apollo.clusterName:default}")
    private String clusterName;

    @Value("${apollo.namespaceName:application}")
    private String namespaceName;

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<String, List<FlowRuleEntity>> converter;

    @Override
    public List<FlowRuleEntity> getRules(String appName) throws Exception {
        RecordLog.info("flowRuleApolloProvider appName----" + appName);
        OpenNamespaceDTO openNamespaceDTO =
                apolloOpenApiClient.getNamespace(appName, env, clusterName, namespaceName);
        String rules = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(flowDataId))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }
}
