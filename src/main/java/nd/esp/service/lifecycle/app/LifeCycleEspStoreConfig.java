package nd.esp.service.lifecycle.app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "nd.esp.service.lifecycle.repository" })
public class LifeCycleEspStoreConfig {

}
