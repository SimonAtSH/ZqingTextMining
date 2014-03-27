package zqing.textmining.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class BaseReader
{

	public String			FileName;
	public FileInputStream	FileStream;
	public StringBuilder	FileContent;

	public BaseReader()
	{}

	public boolean LoadFromFile(String fileName) throws IOException
	{
		FileName = fileName;
		StringBuilder FileContent = new StringBuilder();
		try
		{
			InputStreamReader inputReader = new InputStreamReader(new FileInputStream(fileName), "utf-8");
			BufferedReader bufferReader = new BufferedReader(inputReader);
			String str = null;
			do
			{
				str = bufferReader.readLine();
				if (str != null)
				{
					FileContent.append(str);
					FileContent.append("\n");
				}
			} while (str != null);
			bufferReader.close();
		} catch (IOException e)
		{
			System.out.println("读输入文件错误:" + fileName);
			e.printStackTrace();
		}
		return true;
	}
}
