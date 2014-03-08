package zqing.textmining.output;

import java.util.TreeMap;

import zqing.textmining.entity.WordEntity;

public class CSVExporter extends BaseExporter
{

	public CSVExporter()
	{
		super();
	}
	
	/*
	 * 输出文本行到CSV文件
	 */
	public long ExportLines(String[] lines)
	{
		ExportLinesHeader();
		ExportLinesBody();
		ExportLinesFooter();
		return -1;		
	}
	
	private long ExportLinesHeader()
	{
		return -1;
	}
	
	private long ExportLinesBody()
	{
		return -1;
	}
	
	private long ExportLinesFooter()
	{
		return -1;
	}
	
	/*
	 * 输出词统计矩阵到CSV文件
	 */
	public long ExportWords(TreeMap<String, WordEntity> words)
	{
		return -1;
	}

}
