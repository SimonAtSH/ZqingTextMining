package zqing.textmining.output;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.BufferedWriter;
import java.util.TreeMap;

import zqing.textmining.entity.WordEntity;

public class CSVExporter extends BaseExporter
{

	public CSVExporter()
	{
		super();
	}

	public int					RowCount;
	public int					ColumnCount;
	private String[]	emptyStringArray = new String[]{};

	/*
	 * 输出文本行到CSV文件
	 */
	public boolean ExportLines(String fileName, String[] lines)
	{
		ExportLines(fileName, lines, false);
		return true;
	}
	
	public boolean ExportLines(String fileName, String[] lines, boolean bAppend)
	{
		return this.ExportLines(fileName, lines, bAppend, 0);
	}
	
	public boolean ExportLines(String fileName, String[] lines, boolean bAppend, int minLineChars)
	{
		return this.ExportLines(fileName, lines, bAppend, minLineChars, emptyStringArray);
	}

	public boolean ExportLines(String fileName, String[] lines, boolean bAppend, int minLineChars, String[] ignorePrefix)
	{
		try
		{
			BufferedWriter bufferWriter = Files.newBufferedWriter(Paths.get(fileName), Charset.forName("UTF8"), 
	                StandardOpenOption.WRITE, 
	                bAppend? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING,
	                StandardOpenOption.CREATE);
			for (String s : lines)
			{
				if(s.isEmpty()) continue;
				if(s.length() < minLineChars) continue;
				boolean bIgnore = false;
				for(String ignorePrefixString : ignorePrefix)
				{
					if(s.startsWith(ignorePrefixString)) 
					{
						bIgnore = true;
						break;
					}
				}
				if(bIgnore) continue;
				try
				{
					bufferWriter.write(s + "\r\n");
				}
				catch(Exception ex)
				{
					System.out.println("写某句话错误");
				}
			}
			bufferWriter.close();
		}
		catch (Exception e)
		{
			System.out.println("写输出文件错误");
			e.printStackTrace();
		}
		return true;
	}
	

	/*
	 * 输出词统计矩阵到CSV文件
	 */
	public long ExportWords(TreeMap<String, WordEntity> words)
	{
		return -1;
	}

}
