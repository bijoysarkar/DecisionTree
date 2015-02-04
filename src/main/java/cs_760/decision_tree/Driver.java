package cs_760.decision_tree;

public class Driver {

  public void part1(String training_data, String test_data, int m) {
    DecisionTree decisionTree = new DecisionTree(training_data, test_data, m);
    decisionTree.train();
    decisionTree.print();
    decisionTree.test(true);
  }

  public void part2(String training_data, String test_data) {
    int sample_sizes[] = {25, 50, 100, 200};
    int repeat_count[] = {10, 10, 10, 1};
    for (int i = 0; i < sample_sizes.length; i++) {
      float numerator = 0;
      int sample_size = sample_sizes[i];
      int repeat = repeat_count[i];
      DecisionTree decisionTree = new DecisionTree(training_data, test_data, 4);
      float min = Integer.MAX_VALUE;
      float max = Integer.MIN_VALUE;
      for (int j = 0; j < repeat; j++) {
        decisionTree.train(sample_size);
        float correct = decisionTree.test(false);
        min = (correct < min) ? correct : min;
        max = (correct > max) ? correct : max;
        numerator = numerator + correct;
      }
      System.out.println(sample_size + "\t" + numerator / repeat + "\t" + min + "\t" + max);
    }
  }

  public void part3(String training_data, String test_data) {
    int ms[] = {2, 5, 10, 20};
    for (int m : ms) {
      DecisionTree decisionTree = new DecisionTree(training_data, test_data, m);
      decisionTree.train();
      System.out.println(m + "\t" + decisionTree.test(false));
    }
  }

  public static void main(String[] args) {
    Driver driver = new Driver();
    driver.part1(args[0], args[1], Integer.parseInt(args[2]));
    //driver.part2(args[0], args[1]);
    //driver.part3(args[0], args[1]);

  }
}
