package in.ignishers.eduportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class EduportalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduportalBackendApplication.class, args);
    }

}
