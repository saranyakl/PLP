package cop5556fa17;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
/**
 * 
 * A simple globalLog that can be used to record a trace of 
 * an instrumented program.
 * 
 * The output can be used for grading and debugging.
 * 
 * initLog should be called in every test case prior to executing the class
 *
 */
public class RuntimeLog {

	private StringBuffer sb;

	public static RuntimeLog globalLog;

	public static ArrayList<BufferedImage> globalImageLog;
	
	public static void initLog() {
		globalLog = new RuntimeLog();
		globalLog.sb = new StringBuffer();
		globalImageLog = new ArrayList<BufferedImage>();
	}
	
	public static void globalLogAddImage(BufferedImage bimage) {
		if(globalImageLog!=null)
			globalImageLog.add(bimage);
	}
	
	public static void addImage(BufferedImage bimage) {
		globalImageLog.add(bimage);
	}
	
	public static String getGlobalString() {
		if(globalLog!=null)
			return globalLog.toString();
		else
			return "";
	}
	
	public static void globalLogAddEntry(String entry){
		if (globalLog != null) globalLog.addEntry(entry);
	}
	
	private void addEntry(String entry) {
		sb.append(entry);
	}


	@Override
	public String toString() {
		return sb.toString();
	}

	public static void resetLogToNull() {
		globalLog = null;
		globalImageLog = null;
	}

}
