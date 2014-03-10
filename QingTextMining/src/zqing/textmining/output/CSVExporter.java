package zqing.textmining.output;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
		try
		{
			OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(fileName), "GB2312");
			BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
			for (String s : lines)
			{
				bufferWriter.write(s + "\n");
			}
			bufferWriter.close();
		} catch (Exception e)
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
