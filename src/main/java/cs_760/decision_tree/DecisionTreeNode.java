package cs_760.decision_tree;

import java.util.ArrayList;
import java.util.List;


public class DecisionTreeNode {
  int attributeIndex; // -1 for root
  AttributeValue attributeValue; //Value of the attribute
  int classCounts[]; // counts of classes at this node
  String label; // In case it is a terminal node then this holds the label else maxLabel at node
  boolean terminal;
  List<DecisionTreeNode> children; // List of children of this node


  DecisionTreeNode(int attributeIndex, AttributeValue attributeValue, int classCounts[], String label, boolean terminal) {
    this.attributeIndex = attributeIndex; 
    this.attributeValue = attributeValue;
    this.classCounts = classCounts;
    this.terminal = terminal;
    if (terminal) {
      children = null;
    } else {
      children = new ArrayList<DecisionTreeNode>();
    }
    this.label = label;
  }

  /**
   * Add child to the node.
   * 
   * For printing to be consistent, children should be added in order of the attribute values as
   * specified in the dataset.
   */
  public void addChild(DecisionTreeNode child) {
    if (children != null) {
      children.add(child);
    }
  }

  public String printCounts(int[] counts) {
    StringBuilder sb = new StringBuilder("[");
    for (int count : counts)
      sb.append(count).append(" ");
    sb.setCharAt(sb.length()-1, ']');
    return sb.toString();
  }

  public int getAttributeIndex() {
    return attributeIndex;
  }

  public int[] getClassCounts() {
    return classCounts;
  }

  public String getLabel() {
    return label;
  }

  public boolean isTerminal() {
    return terminal;
  }

  public List<DecisionTreeNode> getChildren() {
    return children;
  }

  public AttributeValue getAttributeValue() {
    return attributeValue;
  }
}
