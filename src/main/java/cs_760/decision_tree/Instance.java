package cs_760.decision_tree;

import java.util.Arrays;

public class Instance {

  private AttributeValue attributeValueList[];
  private int instanceClass;
  
  public Instance(int numAttributes, int instanceClass) {
    attributeValueList = new AttributeValue[numAttributes];
    this.instanceClass = instanceClass;
  }
  
  public void setAttributeValue(int index, AttributeValue attributeValue){
    attributeValueList[index] = attributeValue;
  }
  
  public AttributeValue getAttributeValue(int index){
    return attributeValueList[index];
  }
  
  public int getInstanceClass(){
    return instanceClass;
  }
  
  @Override
  public String toString() {
    return Arrays.toString(attributeValueList) + " - " + instanceClass;
  }
  
}
