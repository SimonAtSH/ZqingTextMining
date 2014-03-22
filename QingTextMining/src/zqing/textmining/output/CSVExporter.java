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
//	private OutputStreamWriter	fileWriter		= null;
//	private BufferedWriter		bufferWriter	= null;

	/*
	 * ����ı��е�CSV�ļ�
	 */
	public boolean ExportLines(String fileName, String[] lines)
	{
		ExportLines(fileName, lines, false);
		return true;
	}

	public boolean ExportLines(String fileName, String[] lines, boolean bAppend)
	{
		try
		{
			BufferedWriter bufferWriter = Files.newBufferedWriter(Paths.get(fileName), Charset.forName("UTF8"), 
	                StandardOpenOption.WRITE, 
	                bAppend? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING,
	                StandardOpenOption.CREATE);
			for (String s : lines)
			{
				bufferWriter.write(s + "\n");
			}
			bufferWriter.close();
		}
		catch (Exception e)
		{
			System.out.println("д����ļ�����");
			e.printStackTrace();
		}
		return true;
	}
	

	/*
	 * �����ͳ�ƾ���CSV�ļ�
	 */
	public long ExportWords(TreeMap<String, WordEntity> words)
	{
		return -1;
	}

}
