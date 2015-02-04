package cs_760.decision_tree;

public class NumericalAttribute implements Attribute {

  private String name;
  private AttributeType type;

  public NumericalAttribute(String name) {
    this.name = name;
    type = AttributeType.NUMERICAL;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public AttributeType type() {
    return type;
  }
  
}
