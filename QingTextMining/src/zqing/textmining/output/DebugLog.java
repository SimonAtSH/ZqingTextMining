package zqing.textmining.output;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLog
{

	public DebugLog()
	{
	}
	
	public static SimpleDateFormat   sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd   hh:mm:ss");

	public static void Log(String msg)
	{
		if (msg != null)
		{
			Date date = new Date();
			System.out.printf( "%s : %s\n", sDateFormat.format(date), msg);
		}
	}

}
