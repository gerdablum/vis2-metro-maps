package at.tuwien.vis2.metromaps;

import at.tuwien.vis2.metromaps.api.paper.PaperMetroDataService;
import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Specifies which interfaces should be injected by the spring boot framework
 */
@Configuration
public class Config {

    /**
     * Choose implementation of MetroDataProvider (can be changed for test cases)
     * @return instance of MetroDataProvider
     */
    @Bean
    public MetroDataProvider metroDataProvider() {
        return new PaperMetroDataService(); // PaperMetroDataService // FakeDataService
    }
}
