package vexpress.pricing;

public class SchedulingRequest {
  private int fromZip;

  private int toZip;

  private double weight;

  public SchedulingRequest(final int fromZip, final int toZip, final double weight) {
    this.fromZip = fromZip;
    this.toZip = toZip;
    this.weight = weight;
  }

  public int getFromZip() {
    return fromZip;
  }

  public void setFromZip(final int fromZip) {
    this.fromZip = fromZip;
  }

  public int getToZip() {
    return toZip;
  }

  public void setToZip(final int toZip) {
    this.toZip = toZip;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(final double weight) {
    this.weight = weight;
  }
}
