package study.zl.compiler;

public class TimeCost {
  public static void main(String...args){
    long start = System.currentTimeMillis();
    try {
    Parser.main(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    long end =  System.currentTimeMillis();
    System.out.println(end - start);
  }
}
