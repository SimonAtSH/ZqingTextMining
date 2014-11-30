package zqing.textmining.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

import com.thoughtworks.xstream.XStream;

public class Configuration implements Serializable
{
	/**
	 * 配置变量
	 */
	private static final long	serialVersionUID	= 1L;
	public String				SourceTextFileName = "";
	public String				SourcePosTextFileName = "";
	public String				SourceNegTextFileName = "";
	public String				SourceExcelFileName = "";
	public String				ResultFilePrefix = "";
	public String				ResultFileName = "";
	public String 				ResultFolder = "";
	public String				TextLinesFileName = "";
	public String				SubTextLinesFileName = "";
	public String				DependencyTreeFileName = "";
	public String				WordsTextFileName = "";
	public String				BigramWordsTextFileName = "";
	public String				AllSVMFileName = "";
	public String				TrainSVMFileName = "";
	public String				TestSVMFileName = "";
	public Boolean				GenerateDependencyTree = false;
	public String				KeyDictFileName = "";
	public String[] 			KeyDict = {};
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
				this.SourceTextFileName = cfg.SourceTextFileName;
				this.ResultFileName = cfg.ResultFileName;
				this.ResultFolder = cfg.ResultFolder;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String GetFileNameNoExt(String fullName)
	{
		String strResult = null;
		File userFile = new File(fullName);
		String fileName = userFile.getName();
		if(!fileName.isEmpty())
			strResult = fileName.replaceFirst("[.][^.]+$", "");
		return strResult;
	}

	public boolean ParseArgs(String[] args)
	{
		boolean bResult = true;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equalsIgnoreCase("-InText") )
			{
				this.SourceTextFileName = args[i + 1];
				this.ResultFilePrefix = GetFileNameNoExt(this.SourceTextFileName);
			}
			if (args[i].equalsIgnoreCase("-InPosText") )
			{
				this.SourcePosTextFileName = args[i + 1];
				this.ResultFilePrefix = GetFileNameNoExt(this.SourcePosTextFileName);
			}
			if (args[i].equalsIgnoreCase("-InNegText") )
			{
				this.SourceNegTextFileName = args[i + 1];
				this.ResultFilePrefix = GetFileNameNoExt(this.SourceNegTextFileName);
			}
			if (args[i].equalsIgnoreCase("-InExcel") )
			{
				this.SourceExcelFileName = args[i + 1];
				this.ResultFilePrefix = GetFileNameNoExt(this.SourceExcelFileName);				
			}
			if (args[i].equalsIgnoreCase("-OUT") )
			{
				this.ResultFileName = args[i + 1];
			}
			if(args[i].equalsIgnoreCase("-KeyDict"))
			{
				this.KeyDictFileName = args[i + 1];
			}
			if(args[i].equalsIgnoreCase("-OutFolder") )
			{
				this.ResultFolder = args[i+1];
				String tmpStr = this.ResultFolder + "/" + this.ResultFilePrefix;
				this.AllSVMFileName = tmpStr + "_AllSVM.txt";
				this.TrainSVMFileName = tmpStr + "_Train.txt";
				this.TestSVMFileName = tmpStr + "_Test.txt";
				this.TextLinesFileName = tmpStr + "_TextLines.txt";
				this.SubTextLinesFileName = tmpStr + "_SubTextLines.txt";
				this.DependencyTreeFileName = tmpStr + "_DependencyTree.xls";
				this.WordsTextFileName = tmpStr + "_Words.Txt";
				this.BigramWordsTextFileName = tmpStr + "_BigramWords.Txt";
			}
			if(args[i].equalsIgnoreCase("-DepTree") )
			{
				this.GenerateDependencyTree = true;
			}
		}

		return bResult;
	}

}
