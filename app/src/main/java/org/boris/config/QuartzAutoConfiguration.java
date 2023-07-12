package org.boris.config;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.quartz.QuartzDeadlineManager;
import org.axonframework.serialization.Serializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.quartz.Scheduler;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class QuartzAutoConfiguration {
    @Bean
    public QuartzDeadlineManager deadlineManager(
            Scheduler scheduler,
            org.axonframework.config.Configuration axonConfiguration,
            Serializer serializer,
            TransactionManager txManager
    ) {
        return QuartzDeadlineManager
                .builder()
                .scheduler(scheduler)
                .scopeAwareProvider(new ConfigurationScopeAwareProvider(axonConfiguration))
                .serializer(serializer)
                .transactionManager(txManager)
                .build();
    }
}
