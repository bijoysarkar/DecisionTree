package cs_760.decision_tree;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class DecisionTree {

  private DecisionTreeNode root;
  private NominalAttribute classes;
  private Attribute[] attributeList;
  
  private int m;
  private final String TRAINING_DATA;
  private final String TEST_DATA;

  private final double LOG2_NORMALIZER = Math.log(2);
  private final NumberFormat formatter = new DecimalFormat("#0.000000");

  public DecisionTree(String trainingData, String testData, int m) {
    this.TRAINING_DATA = trainingData;
    this.TEST_DATA = testData;
    this.m = m;
  }

  public void train() {
    Data training_data = new Data(TRAINING_DATA);
    classes = training_data.getClasses();
    attributeList = training_data.getAttributeList();
    train(training_data.getInstanceList());
  }

  public void train(int sample_size) {
    Data training_data = new Data(TRAINING_DATA);
    classes = training_data.getClasses();
    attributeList = training_data.getAttributeList();
    Instance[] ll = training_data.sample(sample_size);
    //for(Instance ins : ll)
    //  System.out.println(ins);
    train(ll);
  }
  
  public void train(Instance[] training_instances){
    List<Instance> list = new ArrayList<Instance>(Arrays.asList(training_instances));
    root = buildTree(-1, null, list, new HashSet<Integer>(), maxLabel(countClasses(list)));
  }

  public float test(boolean print_out) {
    Data test_data = new Data(TEST_DATA);
    Instance[] list = test_data.getInstanceList();
    int count = 0;
    for (Instance instance : list) {
      String classifyLabel = classify(instance);
      if(print_out)
        System.out.println(classifyLabel + " " + classes.categoryName(instance.getInstanceClass()));
      if (classifyLabel.equals(classes.categoryName(instance.getInstanceClass())))
        count++;
    }
    if(print_out)
      System.out.println(count + " " + list.length);
    return (float)count/test_data.getInstanceList().length;
  }

  public void print() {
    print(root, 0);
  }

  private void print(DecisionTreeNode decisionTreeNode, int level) {
    if (decisionTreeNode.terminal)
      return;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < level; i++)
      sb.append("|\t");

    Attribute attribute = attributeList[decisionTreeNode.getChildren().get(0).attributeIndex];
    switch (attribute.type()) {
      case NOMINAL:
        for (int i = 0; i < decisionTreeNode.getChildren().size(); i++) {
          DecisionTreeNode children = decisionTreeNode.getChildren().get(i);
          System.out.println(sb.toString()
              + attribute.getName()
              + " = "
              + ((NominalAttribute) attribute).categoryName((int) children.getAttributeValue()
                  .value()) + " " + children.printCounts(children.getClassCounts())
              + (children.terminal ? ": " + children.label : ""));
          print(children, level + 1);
        }
        break;
      case NUMERICAL:
        DecisionTreeNode leftChild = decisionTreeNode.getChildren().get(0);
        DecisionTreeNode rightChild = decisionTreeNode.getChildren().get(1);
        System.out.println(sb.toString() + attributeList[leftChild.getAttributeIndex()].getName()
            + " <= " + formatter.format(leftChild.getAttributeValue().value()) + " "
            + leftChild.printCounts(leftChild.getClassCounts())
            + (leftChild.terminal ? ": " + leftChild.label : ""));
        print(leftChild, level + 1);
        System.out.println(sb.toString() + attributeList[rightChild.getAttributeIndex()].getName()
            + " > " + formatter.format(rightChild.getAttributeValue().value()) + " "
            + rightChild.printCounts(rightChild.getClassCounts())
            + (rightChild.terminal ? ": " + rightChild.label : ""));
        print(rightChild, level + 1);
        break;
    }

  }

  public DecisionTreeNode buildTree(int attributeIndex, AttributeValue attributeValue,
      List<Instance> list, Set<Integer> processedList, int parentMaxLabel) {

    int[] counts = countClasses(list);

    // Case : No more instances
    if (list.size() == 0)
      return new DecisionTreeNode(attributeIndex, attributeValue, counts,
          classes.categoryName(parentMaxLabel), true);

    int maxLabelIndex = maxLabel(counts);

    // Case : If less than m number of examples are there in this node then return label as max
    if (list.size() < m)
      return new DecisionTreeNode(attributeIndex, attributeValue, counts,
          classes.categoryName(maxLabelIndex), true);

    double totalEntropy = totalEntropy(counts, list.size());

    // Case : All same label
    if (totalEntropy == 0) {
      for (int i = 0; i < classes.categoryCount(); i++)
        if (counts[i] > 0)
          return new DecisionTreeNode(attributeIndex, attributeValue, counts,
              classes.categoryName(i), true);
    }

    // Case : If all questions finished


    // Case : choose question and build children
    double min_entropy = 1;
    int min_entropy_index = -1;
    Object[] conditionalEntropies = new Object[attributeList.length];
    for (int i = 0; i < attributeList.length; i++) {
      Attribute attribute = attributeList[i];
      if (attribute.type() == AttributeType.NOMINAL) {
        double cur_entropy = 1;
        if (!processedList.contains(i))
          cur_entropy = conditionalEntropyNominal(list, i);
        conditionalEntropies[i] = cur_entropy;
        if ((min_entropy - cur_entropy) > 0.0000001) {
          min_entropy = cur_entropy;
          min_entropy_index = i;
        }
      } else {
        NumericAttributeSplit numericAttributeSplit = conditionalEntropyNumerical(list, i);
        conditionalEntropies[i] = numericAttributeSplit;
        if ((min_entropy - numericAttributeSplit.getEntropy()) > 0.0000001) {
          min_entropy = numericAttributeSplit.getEntropy();
          min_entropy_index = i;
        }
      }
    }

    if (min_entropy < totalEntropy) {
      Attribute attribute = attributeList[min_entropy_index];
      DecisionTreeNode decisionTreeNode =
          new DecisionTreeNode(attributeIndex, attributeValue, counts,
              classes.categoryName(maxLabelIndex), false);
      List<List<Instance>> listOfLists;
      switch (attribute.type()) {
        case NOMINAL:
          processedList.add(min_entropy_index);
          listOfLists = splitNominal(list, min_entropy_index);
          int categoryCount = ((NominalAttribute) attribute).categoryCount();
          for (int i = 0; i < categoryCount; i++) {
            List<Instance> splitList = listOfLists.get(i);
            decisionTreeNode.addChild(buildTree(min_entropy_index, new NominalAttributeValue(i),
                splitList, new HashSet<Integer>(processedList), maxLabelIndex));
          }
          break;
        case NUMERICAL:
          NumericAttributeSplit numericAttributeSplit =
              (NumericAttributeSplit) conditionalEntropies[min_entropy_index];
          listOfLists =
              splitNumerical(list, min_entropy_index, numericAttributeSplit.getSplitValue());
          NumericalAttributeValue numericalAttributeValue =
              new NumericalAttributeValue(numericAttributeSplit.getSplitValue());
          decisionTreeNode.addChild(buildTree(min_entropy_index, numericalAttributeValue,
              listOfLists.get(0), new HashSet<Integer>(processedList), maxLabelIndex));
          decisionTreeNode.addChild(buildTree(min_entropy_index, numericalAttributeValue,
              listOfLists.get(1), new HashSet<Integer>(processedList), maxLabelIndex));
          break;
      }
      return decisionTreeNode;
    }
    // TODO
    return new DecisionTreeNode(attributeIndex, attributeValue, counts,
        classes.categoryName(parentMaxLabel), true);
  }

  private int[] countClasses(List<Instance> list) {
    int counts[] = new int[classes.categoryCount()];
    Iterator<Instance> it = list.iterator();
    while (it.hasNext()) {
      Instance instance = it.next();
      counts[instance.getInstanceClass()]++;
    }
    return counts;
  }

  private int maxLabel(int[] counts) {
    int counter = 0;
    int max = counts[counter];
    for (int i = 1; i < classes.categoryCount(); i++) {
      if (counts[i] > max) {
        counter = i;
        max = counts[counter];
      }
    }
    return counter;
  }

  private double totalEntropy(int[] counts, int totalCount) {
    double entropy = 0;
    for (int count : counts)
      entropy = entropy + calculateEntropyFraction(count, totalCount);
    // TODO
    return entropy / LOG2_NORMALIZER;
  }

  private double conditionalEntropyNominal(List<Instance> list, int attributeIndex) {
    double entropy = 0;
    int size = list.size();
    if (size > 0) {
      int categoryCount = ((NominalAttribute) attributeList[attributeIndex]).categoryCount();

      int counts[] = new int[categoryCount];
      int perCategoryClassCounts[][] = new int[categoryCount][classes.categoryCount()];

      for (Instance instance : list) {
        int categoryIndex = (int) instance.getAttributeValue(attributeIndex).value();
        counts[categoryIndex]++;
        perCategoryClassCounts[categoryIndex][instance.getInstanceClass()]++;
      }

      for (int i = 0; i < categoryCount; i++) {
        int count = counts[i];

        // If all of same category for this attribute then ...
        if (count == size)
          return 1;

        if (count > 0) {
          double probability = (double) count / size;
          double categoryConditionalEntropy = 0;
          for (int classCount : perCategoryClassCounts[i])
            categoryConditionalEntropy =
                categoryConditionalEntropy + calculateEntropyFraction(classCount, count);
          entropy = entropy + probability * categoryConditionalEntropy;
        }
      }
    }
    // TODO
    return entropy / LOG2_NORMALIZER;
  }

  private NumericAttributeSplit conditionalEntropyNumerical(List<Instance> list, int attributeIndex) {
    // If all same value then entropy is set to 1 and split value is NaN
    NumericAttributeSplit split = new NumericAttributeSplit();
    int size = list.size();
    if (size > 0) {

      // Count frequency of instances for same numeric AttributeValue
      int[] classCount = new int[classes.categoryCount()];
      Map<Double, int[]> map = new TreeMap<Double, int[]>();

      for (Instance instance : list) {
        classCount[instance.getInstanceClass()]++;
        double value = (double) instance.getAttributeValue(attributeIndex).value();
        int[] counts = map.get(value);
        if (counts == null)
          map.put(value, (counts = new int[classes.categoryCount()]));
        counts[instance.getInstanceClass()]++;
      }

      // Calculate conditional entropy for all valid splits
      int cumulativePrevTotalCount = 0;
      int[] cumulativePrevClassCount = new int[classes.categoryCount()];
      Iterator<Entry<Double, int[]>> it = map.entrySet().iterator();
      Entry<Double, int[]> prevEntry, currEntry;

      if (it.hasNext()) {
        prevEntry = it.next();
        while (it.hasNext()) {
          int[] prevCounts = prevEntry.getValue();
          // Cumulative count of instances less than this split
          for (int i = 0; i < classes.categoryCount(); i++) {
            cumulativePrevClassCount[i] = cumulativePrevClassCount[i] + prevCounts[i];
            cumulativePrevTotalCount = cumulativePrevTotalCount + prevCounts[i];
          }
          currEntry = it.next();
          if (!matchClassLabels(prevEntry.getValue(), currEntry.getValue())) {
            double currentSplitEntropyLess = 0;
            double currentSplitEntropyMore = 0;

            for (int i = 0; i < classes.categoryCount(); i++) {
              currentSplitEntropyLess =
                  currentSplitEntropyLess
                      + calculateEntropyFraction(cumulativePrevClassCount[i],
                          cumulativePrevTotalCount);
              currentSplitEntropyMore =
                  currentSplitEntropyMore
                      + calculateEntropyFraction((classCount[i] - cumulativePrevClassCount[i]),
                          (size - cumulativePrevTotalCount));
            }
            double probLess = (double) cumulativePrevTotalCount / size;
            double currentSplitEntropy =
                probLess * currentSplitEntropyLess + (1 - probLess) * currentSplitEntropyMore;
            if (currentSplitEntropy < split.getEntropy()) {
              split.setEntropy(currentSplitEntropy);
              split.setSplitValue((prevEntry.getKey() + currEntry.getKey()) / 2);
            }
          }
          prevEntry = currEntry;
        }
      }
    }
    // TODO
    split.setEntropy(split.getEntropy() / LOG2_NORMALIZER);
    return split;
  }

  private double calculateEntropyFraction(int numerator, int denominator) {
    if (numerator > 0) {
      double frac = (double) numerator / denominator;
      // TODO
      return -(frac * Math.log(frac));
    } else {
      return 0;
    }
  }

  private boolean matchClassLabels(int label_counts1[], int label_counts2[]) {
    // Considering the label counts are for the same attribute
    int non_zero = 0;
    for (int i = 0; i < label_counts1.length; i++) {
      if (label_counts1[i] * label_counts2[i] > 0) {
        non_zero++;
      } else if (label_counts1[i] + label_counts2[i] > 0) {
        return false;
      }
    }
    return (non_zero == 1);
  }

  private List<List<Instance>> splitNominal(List<Instance> list, int attributeIndex) {
    List<List<Instance>> listOfLists = new ArrayList<List<Instance>>();

    NominalAttribute nominalAttribute = (NominalAttribute) attributeList[attributeIndex];
    int categoryCount = nominalAttribute.categoryCount();
    for (int i = 0; i < categoryCount; i++)
      listOfLists.add(new ArrayList<Instance>());

    for (Instance instance : list)
      listOfLists.get((int) instance.getAttributeValue(attributeIndex).value()).add(instance);

    return listOfLists;
  }

  private List<List<Instance>> splitNumerical(List<Instance> list, int attributeIndex,
      double splitValue) {
    List<List<Instance>> listOfLists = new ArrayList<List<Instance>>();
    listOfLists.add(new ArrayList<Instance>());
    listOfLists.add(new ArrayList<Instance>());
    for (Instance instance : list) {
      if ((double) instance.getAttributeValue(attributeIndex).value() <= splitValue)
        listOfLists.get(0).add(instance);
      else
        listOfLists.get(1).add(instance);
    }
    return listOfLists;
  }

  public String classify(Instance instance) {
    DecisionTreeNode current_node = root;
    while (!current_node.terminal) {
      int attributeIndex = current_node.getChildren().get(0).attributeIndex;
      AttributeValue attributeValue = instance.getAttributeValue(attributeIndex);
      switch (attributeList[attributeIndex].type()) {
        case NOMINAL:
          int childNumber = (int) attributeValue.value();
          current_node = current_node.children.get(childNumber);
          break;
        case NUMERICAL:
          double value = (double) attributeValue.value();
          if (value <= ((double) current_node.children.get(0).getAttributeValue().value())) {
            current_node = current_node.children.get(0);
          } else {
            current_node = current_node.children.get(1);
          }
          break;
      }
    }
    return current_node.label;
  }

}
