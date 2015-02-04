package cs_760.decision_tree;

public class NumericalAttributeValue implements AttributeValue<Double> {

  public double value;

  public NumericalAttributeValue(double value) {
    this.value = value;
  }

  @Override
  public Double value(){
    return value;
  }

  @Override
  public String toString() {
    return "" + value ;
  }
  
}
