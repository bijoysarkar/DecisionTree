package cs_760.decision_tree;

public class NumericAttributeSplit {

  private double splitValue;
  private double entropy;

  public NumericAttributeSplit() {
    splitValue = Double.NaN;
    entropy = 1;
  }

  public NumericAttributeSplit(double splitValue, double entropy) {
    this.splitValue = splitValue;
    this.entropy = entropy;
  }

  public double getSplitValue() {
    return splitValue;
  }

  public void setSplitValue(double splitValue) {
    this.splitValue = splitValue;
  }

  public double getEntropy() {
    return entropy;
  }

  public void setEntropy(double entropy) {
    this.entropy = entropy;
  }

  @Override
  public String toString() {
    return ""+entropy;
  }
}
