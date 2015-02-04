package cs_760.decision_tree;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class NominalAttribute implements Attribute {

  private String name;
  private AttributeType type;
  private String categories[];
  private Map<String, Integer> categoryMap;

  public NominalAttribute(String name, int numValues, Enumeration<String> enumeration) {
    this.name = name;
    type = AttributeType.NOMINAL;
    categories = new String[numValues];
    categoryMap = new HashMap<String, Integer>();
    int counter = -1;
    while (enumeration.hasMoreElements()) {
      categories[++counter] = enumeration.nextElement();
      categoryMap.put(categories[counter], counter);
    }
  }

  public String categoryName(int n){
    return categories[n];
  }
  
  public int categoryCount(){
    return categories.length;
  }
  
  public Integer getAttribute(String attribute) {
    return categoryMap.get(attribute);
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
