package at.tuwien.vis2.metromaps;

import at.tuwien.vis2.metromaps.api.FakeDataService;
import at.tuwien.vis2.metromaps.api.M10Service;
import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class Config {

    @Bean
    public MetroDataProvider metroDataProvider(@Value("classpath:exports/UBAHNOGD_UBAHNHALTOGD.json") Resource data) {
        return new M10Service(data);
    }
}
