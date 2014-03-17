package zqing.textmining.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

import com.thoughtworks.xstream.XStream;

public class Configuration implements Serializable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	public String				SourceFileName;
	public String				SourceExcelFileName;
	public String				ResultFileName;
	public String 				ResultFolder;
	public static String		Encode = "UTF-8";

	private Configuration()
	{}

	private static volatile Configuration	instance;

	public static Configuration getInstance()
	{
		if (instance == null)// 1
			synchronized (Configuration.class)
			{// 2
				if (instance == null)// 3
					instance = new Configuration();
			}
		return instance;
	}

	public void SerializeToXml()
	{
		XStream xStream = new XStream();
		xStream.alias("Configuration", Configuration.class);
		try
		{
			FileOutputStream foStream = new FileOutputStream(
					"Configuration.xml");
			xStream.toXML(this, foStream);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void DeSerializeFromXml()
	{
		XStream xStream = new XStream();
		xStream.alias("Configuration", Configuration.class);
		Configuration cfg = null;
		try
		{
			FileInputStream flStream = new FileInputStream("Configuration.xml");
			cfg = (Configuration) xStream.fromXML(flStream);
			if (cfg != null)
			{
				this.SourceFileName = cfg.SourceFileName;
				this.ResultFileName = cfg.ResultFileName;
				this.ResultFolder = cfg.ResultFolder;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void ParseArgs(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equalsIgnoreCase("-In") )
			{
				this.SourceFileName = args[i + 1];
			}
			if (args[i].equalsIgnoreCase("-InExcel") )
			{
				this.SourceExcelFileName = args[i + 1];
			}
			if (args[i].equalsIgnoreCase("-OUT") )
			{
				this.ResultFileName = args[i + 1];
			}
			
			if(args[i].equalsIgnoreCase("-OutFolder") )
			{
				this.ResultFolder = args[i+1];
			}
		}
	}

}
