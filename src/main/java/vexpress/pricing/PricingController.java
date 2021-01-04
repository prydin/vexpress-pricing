package vexpress.pricing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class PricingController {
  @Autowired private Config config;

  @GetMapping("/health")
  public String healthCheck() {
    return "OK";
  }

  @PostMapping(value = "/price", consumes = "application/json", produces = "application/json")
  public double getPrice(
      @RequestParam final String fromZip,
      @RequestParam final String toZip,
      @RequestParam final double weight) {

    final RestTemplate restTemplate = new RestTemplate();
    final UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(config.getZipcodeUrl() + "/distance")
            .queryParam("fromZip", fromZip)
            .queryParam("toZip", toZip);
    final ResponseEntity<DistanceResponse> response =
        restTemplate.getForEntity(builder.build().toUri(), DistanceResponse.class);
    return weight * Math.PI + 0.02 * response.getBody().getDistance();
  }
}
