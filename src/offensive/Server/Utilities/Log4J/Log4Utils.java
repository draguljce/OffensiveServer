package offensive.Server.Utilities.Log4J;

import java.util.Enumeration;

import offensive.Server.Utilities.Constants;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class Log4Utils {
	
	public static Logger getLogger(String logPath) {
		Logger logger = Logger.getLogger(offensive.Server.Server.class);
		
		if(logPath == null || logPath.isEmpty()) {
			logPath = Constants.DefaultLogPath;
		}
		
		@SuppressWarnings("unchecked")
		Enumeration<Appender> appenders = logger.getAllAppenders();
		
		while(appenders.hasMoreElements()) {
			Appender appender = appenders.nextElement();
			
			if(appender instanceof FileAppender) {
				FileAppender fileAppender = (FileAppender)appender;
				
				fileAppender.setFile(logPath);
			}
		}
		
		return logger;
	}
}
