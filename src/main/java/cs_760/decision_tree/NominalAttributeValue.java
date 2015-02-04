package cs_760.decision_tree;

public class NominalAttributeValue implements AttributeValue<Integer> {

  public int value;

  public NominalAttributeValue(int value) {
    this.value = value;
  }

  @Override
  public Integer value(){
    return value;
  }

  @Override
  public String toString() {
    return "" + value ;
  }
  
}
