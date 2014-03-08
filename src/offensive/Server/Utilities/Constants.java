package offensive.Server.Utilities;

import offensive.Server.Hybernate.POJO.OffensiveUser;

public class Constants {
	public static final String OffensiveUserType = "OffensiveUser";
	
	public static final String FacebookUserType = "FacebookUser";
	
	public static final String OffensiveUserClassName = OffensiveUser.class.getName();
	
	public static final String LogPathVarName = "LogPath";
	
	public static final String DefaultLogPath = "C:\\offensive\\server\\logs\\osrv.log";
	
	public static final String ConfigFilePathVarName = "ConfigFilePath";
	
	public static final String HandlerThreadNumVarName = "NumberOfHandlerThreads";
	
	public static final String BattleThreadNumVarName = "NumberOfBattleThreads";
	
	public static final String RegistrationThreadNumVarName = "NumberOfRegistrationThreads";
	
	public static final String RegistrationThreadNumDefaultVal = "3";
			
	public static final String HandlerThreadNumDefaultVal = "5";
	
	public static final String BattleThreadNumDefaultVal = "4";
	
	public static final String PortNumberVarName = "PortNumber";
	
	public static final String PortNumberDefaultVal = "1973";
	
	public static final String RegistrationPortNumberVarName = "RegistrationPortNumber";
	
	public static final String RegistrationPortNumberDefaultVal = "8080";
	
	public static final String ServerSocketTimeoutVarName = "ServerSocketTimout";
	
	public static final String ServerSocketTimeoutDefaultVal = "0";
	
	public static final String SocketTimeoutVarName = "SocketTimout";
	
	public static final String SocketTimeoutDefaultVal = "1000";
}
