/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren;

import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Map;


@SpringBootApplication(exclude={SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
public class AdminApplication {
	Logger logger = LoggerFactory.getLogger(AdminApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}

    @Bean
    public Connector processTextConnector() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            String contentToProcess = (String) inBoundVariables.get("content");
            // Logic Here to decide if content is approved or not
            if (contentToProcess.contains("activiti")) {
                logger.info("> Approving content: " + contentToProcess);
                integrationContext.addOutBoundVariable("approved",
                        true);
            } else {
                logger.info("> Discarding content: " + contentToProcess);
                integrationContext.addOutBoundVariable("approved",
                        false);
            }
            return integrationContext;
        };
    }

    @Bean
    public Connector tagTextConnector() {
        return integrationContext -> {
            String contentToTag = (String) integrationContext.getInBoundVariables().get("content");
            contentToTag += " :) ";
            integrationContext.addOutBoundVariable("content",
                    contentToTag);
            logger.info("Final Content: " + contentToTag);
            return integrationContext;
        };
    }

    @Bean
    public Connector discardTextConnector() {
        return integrationContext -> {
            String contentToDiscard = (String) integrationContext.getInBoundVariables().get("content");
            contentToDiscard += " :( ";
            integrationContext.addOutBoundVariable("content",
                    contentToDiscard);
            logger.info("Final Content: " + contentToDiscard);
            return integrationContext;
        };
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCompletedEvent> processCompletedListener() {
        return processCompleted -> logger.info(">>> Process Completed: '"
                + processCompleted.getEntity().getName() +
                "' We can send a notification to the initiator: " + processCompleted.getEntity().getInitiator());
    }

//    @Bean
//      public SpringProcessEngineConfiguration processEngineConfiguration(DataSource dataSource, DataSourceTransactionManager transactionManager){
//        SpringProcessEngineConfiguration springProcessEngineConfiguration = new SpringProcessEngineConfiguration();
//        springProcessEngineConfiguration.setDataSource(dataSource);
//        springProcessEngineConfiguration.setTransactionManager(transactionManager);
//        springProcessEngineConfiguration.setDatabaseSchemaUpdate("true");
//        springProcessEngineConfiguration.setDatabaseSchema("mysql");
//        return springProcessEngineConfiguration;
//      }
//
//    @Bean
//    public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration processEngineConfiguration){
//        ProcessEngineFactoryBean bean = new ProcessEngineFactoryBean();
//        bean.setProcessEngineConfiguration(processEngineConfiguration);
//        return bean;
//    }
}
