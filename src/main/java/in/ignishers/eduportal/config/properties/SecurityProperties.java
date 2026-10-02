package in.ignishers.eduportal.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private List<String> publicUrls = new ArrayList<>();
}
