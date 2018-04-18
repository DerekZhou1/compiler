package study.zl.compiler;

import java.io.IOException;

public class Token {

  int type;
  String text;
  String[] array ={"EOF", "EQUALS", "COMMA", "LPARENT", "RPARENT", "Name"};

  //当实例化一个token后，指针会自动前移
  public Token(int type, String text) {
    this.type = type;
    this.text = text;
  }

  public void tostring(){
    System.out.println("<\'" + text + "\' : " + array[type] + ">");

  }
}