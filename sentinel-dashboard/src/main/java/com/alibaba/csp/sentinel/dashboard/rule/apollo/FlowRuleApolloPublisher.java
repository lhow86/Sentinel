package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("flowRuleApolloPublisher")
public class FlowRuleApolloPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;

    @Autowired
    private Converter<List<FlowRuleEntity>, String> converter;

    @Value("${apollo.env:FAT}")
    private String env;

    @Value("${apollo.flowDataId:sentinel.flowRules}")
    private String flowDataId;

    @Value("${apollo.clusterName:default}")
    private String clusterName;

    @Value("${apollo.namespaceName:application}")
    private String namespaceName;

    @Override
    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
        RecordLog.info("flowRuleApolloPublisher app----" + app);
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        //TODO 处理不兼容的字段，spring cloud alibaba 0.2.2版本中实现JSON转换的时候，不会忽略不存在的字段，
        // 会导致客户端出现出现异常,可以通过下面这段代码将这些字段不存入Apollo，以避免客户端加载的错误
//        for (FlowRuleEntity ruleEntity : rules) {
//            ruleEntity.setId(null);
//            ruleEntity.setApp(null);
//            ruleEntity.setGmtModified(null);
//            ruleEntity.setGmtCreate(null);
//            ruleEntity.setIp(null);
//            ruleEntity.setPort(null);
//        }
        // 视情况使用

        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(converter.convert(rules));
        openItemDTO.setComment("modify by sentinel-dashboard");
        openItemDTO.setDataChangeCreatedBy("apollo");
        apolloOpenApiClient.createOrUpdateItem(app, env, clusterName, namespaceName, openItemDTO);

        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("release by sentinel-dashboard");
        namespaceReleaseDTO.setReleasedBy("apollo");
        namespaceReleaseDTO.setReleaseTitle("release by sentinel-dashboard");
        apolloOpenApiClient.publishNamespace(app, env, clusterName, namespaceName, namespaceReleaseDTO);
    }
}
