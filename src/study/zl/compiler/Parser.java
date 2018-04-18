package study.zl.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xpath.internal.operations.Bool;



public class Parser {


	public static String path = System.getProperty("user.dir") + File.separator + "data" 
	    + File.separator + "text";

	public static  void main(String...args) throws Exception {
		File file = new File(path);
		if(file.exists()) {
			try {
				FileReader fr = new FileReader(file);
				char[] array = new char[100];
				Arrays.fill(array, ' ');
//				fr.read(array);
				Lexer lexer = new Lexer(fr);
				//				System.out.println(String.valueOf(array));
				//				lexer.Print();
				//				lexer.Print();
				Parser parser = new Parser(lexer);
				parser.stat();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	Lexer lexer;
	
	
	static final int K = 2; //向前看多少个token
	
	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}

	
	public void stat() throws Exception {
	  if(speculateAssign())  {
	    memoMap.clear();
	    assign();
	  } else if(speculateList()) {
	    memoMap.clear();
      list();
    }
	  if(!match(Lexer.EOF)) {
	    throw new RecognitionException("expecting EOF .found: " + LT(0).text);
	  }
	}
	
	/**推演list是否能匹配成功，如果成功则返回true，推演无论失败与否match指针都会回到退眼前指向的位置
	 * @return
	 */
	Boolean speculateList(){
	  Boolean success = true;
    markup();
    try{
      memoList();
      
    } catch (RecognitionException e) {
      success = false;
    } 
    recover();
    return success;
	}
	
	/**推演list是否能匹配成功，如果成功则返回true，推演无论失败与否match指针都会回到退眼前指向的位置
	 * @return
	 */
	Boolean speculateAssign() {
	  Boolean success = true;
	  markup();
	  try{
      assign();
      
    } catch (RecognitionException e) {
      success = false;
    }
	  recover();
	  return success;
	}
	
	Boolean speculationMode = false;
//	int store = 0;
	
	
	/**标记推演模式开启
	 * 
	 */
	void markup() {
	  speculationMode = true;
	 
	}
	

	/**标记推演模式关闭，并将指针重置
	 * 
	 */
	void recover() {
	  speculationMode = false;
	  index = 0;
	}
	
/**
 * map用于添加记忆机制，避免一种解析模式推演一次后另一种解析模式重复推演。map中key记录推演时的token数组
 * 下标，如果推演失败value就是-1，如果推演成功value就是推演的一串token的末尾的下标。
 * 推演不管成功还是失败都会保存在map中。
 * 一个语法单元的解析方式确定后，此时进入非推演模式，要清空map
 *
 */
	Map<Integer,Integer> memoMap = new HashMap<Integer, Integer>();
	
	/**添加记忆机制，管理map，判断是否需要推演。
	 * @throws Exception
	 */
	void memoList() throws RecognitionException {
	  Boolean failed = true;
	  int startIndex = index;
	  if(speculationMode && alreadySpeculator()) {
	  System.out.println("SKIP:" + startIndex + "to" + index + ";" 
	      +  "to" + LT(-1).text);
	    return ;
	  }
	  try {
	    list();
	    failed = false;
	  } catch (RecognitionException e) {
	    throw e;
	  } finally {
	    memorize(failed,startIndex);
	  }
	}
	

	
	/**查看是否已经推演过，如果已经过返回true，并迁移指针;否则返回false，
	 * @return
	 */
	boolean alreadySpeculator(){
	  if(memoMap.isEmpty()) {
	    return false;
	  }
	  int failed = memoMap.get(index);
	  if(failed == -1) {
	    return false;
	  } else {
	    index = failed + 1;
	    return true;
	  }
	}
	
	void memorize(boolean failed ,int start){
	  if(failed){
	    memoMap.put(start, -1);
	  } else {
	    memoMap.put(start, index-1);
	  }
	}
	
	
	void assign() throws RecognitionException {
	  memoList();
	  if(!match(Lexer.EQUALS)) {
	    throw new RecognitionException("expecting name ; found:" + LT(0).text);
	  }
	  memoList();
	}
	
	void list() throws RecognitionException {
		if(match(Lexer.LBRACK)) {
			elements();
			if( !match(Lexer.RBRACK)) {
				throw new RecognitionException("must end with token \']\'; found:" + LT(0).text);
			}
		} else {
			throw new RecognitionException("must start with token \'[\'; found:" + LT(0).text);
		}
	}

	void elements() throws RecognitionException {
		element();
		while(match(Lexer.COMMA)) {
			element();
		}
	}

	void element() throws RecognitionException {
		if(LA(0) == Lexer.NAME && LA(1) == Lexer.EQUALS) {

		    match(Lexer.NAME);
		    match(Lexer.EQUALS);
		    if(!match(Lexer.NAME)) {
		      throw new RecognitionException("expecting name ; found:" + LT(0).text);
		    }
			
		} else if (LA(0) == Lexer.NAME ) {
		  match(Lexer.NAME);
		} else if (LA(0) == Lexer.LBRACK) {
		  list();
		} else {
		  throw new  RecognitionException("expecting name or list ; found:" + LT(0).text);
		}
	}
	
	 static int LINE =1;
//	 Token token = null;
	 
	 
	 //示例写法是将lookAhead做成一个循环缓冲区,算下标时每次都是加1取模，我这里时每次都删除第一个元素，在末尾添加元素，同样可以保持size在某一大小不变
	 //这是利用了List接口的自动重排下标功能，效率可能会降低，但影响不大。优点是取值时比较方便，使用模式五的推演功能时可以方便的自增list长度。
	 //备注： lookAhead长度只会变长，不会变短。比如默认k是2，经过推演后lookAhead长度变为m，则后面则变成一个缓冲区为m的解析法LL(m),
	 List<Token>  lookAhead = new ArrayList<Token>();
	 int index = 0; //缓冲区指针
	 
	//如果匹配则词法单元流指针前移一位，返回true，否则返回false，指针不前移（这里是token指针，不是char指针）
	Boolean match(int tokenNum) throws RecognitionException {
	  if(lookAhead.size() == 0) {  //初始化lookAhead
	   
	    for(int i = 0; i < K; i++) {
	      lookAhead.add(Lexer.nextToken());
	    }
	  }
	  if (!speculationMode) {
//	    在非推演模式时，将
	    Token temp = LT(0);
	    if(temp.type == tokenNum) {
	      System.out.print(LINE++ + ": ");
	      temp.tostring();

	      lookAhead.remove(0);
	      lookAhead.add(Lexer.nextToken());
	      return true;

	    } else {
	      return false;
	    }
	  } else {
	    Token temp = lookAhead.get(index);
	    if(temp.type == tokenNum) {
	      lookAhead.add(Lexer.nextToken());
	      index++;
	      return true;
	    } else {
	      return false;
	    }
	  }
	}
	
	
	/**
	 * @param i 当前缓冲区指针后i个token
	 * @return 返回当前缓冲区后i个token 的类型，如果当前index为0，i为1，则 lookAhead（1）的类型
	 */
	int LA(int i) {
	  return lookAhead.get(index + i).type;
	}
	
	/**
   * @param i 当前缓冲区指针后i个token，
   * @return 返回当前缓冲区后i个token 的类型，如果当前index为0，i为1，则 返回lookAhead（1）
   */
  Token LT(int i) {
    return lookAhead.get(index + i);
  }
}

class RecognitionException extends Exception {
  public RecognitionException(String message) {
   super(message);
  }
}



