package vexpress.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(Config.class)
public class PricingApplication {

  public static void main(final String[] args) {
    SpringApplication.run(PricingApplication.class, args);
  }
}
