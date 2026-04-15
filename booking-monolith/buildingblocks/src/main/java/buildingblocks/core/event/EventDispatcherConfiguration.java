package buildingblocks.core.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventDispatcherConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventDispatcher eventDispatcher(EventMapper eventMapper, ApplicationEventPublisher applicationEventPublisher, ApplicationContext applicationContext) {
        return new EventDispatcherImpl(eventMapper, applicationEventPublisher, applicationContext);
    }
}
