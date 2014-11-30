package zqing.textmining.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import zqing.textmining.output.DebugLog;

public class TextReader extends BaseReader
{

	public TextReader()
	{
		super();		
	}
	
	public String[] LoadFromFile(String fileName) throws IOException
	{
		FileName = fileName;
		ArrayList<String> lines = new ArrayList<String>();
		try
		{
			DebugLog.Log("开始加载文件:"+ fileName);
			InputStreamReader inputReader = new InputStreamReader(new FileInputStream(fileName), "utf-8");
			BufferedReader bufferReader = new BufferedReader(inputReader);
			String str = null;
			do
			{
				str = bufferReader.readLine();
				if (str != null)
				{
					lines.add(str);
				}
			} while (str != null);
			DebugLog.Log("完成加载文件:"+ fileName);
			bufferReader.close();
		} catch (IOException e)
		{
			System.out.println("读输入文件错误:" + fileName);
			e.printStackTrace();
		}
		return lines.toArray(new String[0]);
	}

}
