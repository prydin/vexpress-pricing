package vexpress.pricing;

// TODO: Move this to common package and share with zipcode module
public class DistanceResponse {

  private double distance;

  private String unit;

  public DistanceResponse() {}

  public DistanceResponse(final double distance, final String unit) {
    this.distance = distance;
    this.unit = unit;
  }

  public double getDistance() {
    return distance;
  }

  public String getUnit() {
    return unit;
  }

  public void setDistance(final double distance) {
    this.distance = distance;
  }

  public void setUnit(final String unit) {
    this.unit = unit;
  }
}
