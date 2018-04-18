package study.zl.compiler;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Lexer{
  static FileReader FR =null;
  //	static lint index =0;
  //这里的C保存每次从文件中读取的字符，没有采用char 是因为EOF字符从文件读取后再通过强制转换（int）C  值为65535 并不是-1 ,感觉会丢失信息，所以
  static char C = 0;
  //	List<Token> tokens = new ArrayList<Token>();
  final static int EOF = 0;
  final static int EQUALS = 1;  //所有需要跳过的符号。如空格， 换行，制表符
  final static int COMMA = 2;
  final static int LBRACK = 3; //左中括号
  final static int RBRACK = 4;
  final static int NAME = 5;	//所有连续的字母组成的字符串都是一个NAME

  public Lexer(FileReader fr) throws IOException {
    this.FR = fr;
    consume();
  }

  

  public static Token nextToken() throws RecognitionException {
    if(C == 65535) {
      return new Token(EOF, "-1");
    }
    switch(C) {
    case ' ': 
    case '\r':
    case '\n': 
    case '\t': skipBlank();return nextToken();
    case '=': consume(); return new Token(EQUALS, "=");
    case ',': consume(); return new Token(COMMA, ",");
    case '[': consume(); return new Token(LBRACK, "[");
    case ']': consume(); return new Token(RBRACK, "]");
    default:
      return new Token(NAME, getName());
    }
  }


  
//跳过空格换行等
  static  void skipBlank() {
    do{
      consume();
      if((int)C == 65535) { 
        break;
      }
    }while((C == ' ' || C == '\r' || C == '\n' || C == '\t'));
    
  }
  
  //数组下标自增
  static void consume()  {
    try {
      C = (char)FR.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  } 

  //当检测到字母时，找到整个字符串
  static String getName()   {
    StringBuffer sb =new StringBuffer();
   
    do{

      if((int)C == 65535) {
        break;
      }
      sb.append(C);
      consume();
    }while((C >= 'a' && C <= 'z')||(C >= 'A' && C <= 'Z'));
    String name = sb.toString();
    return name;
  }
  
  
  public static void test() {
    System.out.println("11");
  }
}

