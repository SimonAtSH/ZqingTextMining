package zqing.textmining.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CSVReader extends BaseReader
{
	public CSVReader()
	{
		super();
	}

	public ArrayList<String>			Lines;
	public ArrayList<ArrayList<String>>	FieldsList;
	public String[][] Fields;
	public int RowCount;
	public int ColumnCount;

	private InputStreamReader	fileReader		= null;
	private BufferedReader		bufferReader	= null;

	public boolean LoadFromFile(String fileName) throws IOException
	{
		FileName = fileName;
		String strLine = null;
		RowCount = 0; 
		ColumnCount = 0;
		try
		{
			fileReader = new InputStreamReader(new FileInputStream(fileName),"utf-8");
			bufferReader = new BufferedReader(fileReader);
			Lines = new ArrayList<String>();
			FieldsList = new ArrayList<ArrayList<String>>();
			// 读取一行
			while ((strLine = bufferReader.readLine()) != null)
			{
				Lines.add(strLine);				
				ArrayList<String> cells = new ArrayList<String>();// 每行记录一个list
				String[] cs =  strLine.split("\t");
				// 读取每个单元格
				for(String s:cs)
				{
					cells.add(s);
				}
				FieldsList.add(cells);
				if(cells.size()>ColumnCount) ColumnCount = cells.size();
			}
			RowCount = FieldsList.size();
			//将ArrayList<ArrayList<String>> 转化为String[][]，以方便使用各个字段的值。
			Fields = new String[RowCount][ColumnCount];
			for(int i = 0; i < RowCount; i++)
			{
				ArrayList<String> line =FieldsList.get(i); 
				int iCol = line.size();
				for(int j = 0; j < iCol; j++)
				{
					Fields[i][j] = line.get(j); 
				}
			}			
		} catch (IOException e)
		{
			System.out.println("读输入文件错误:" + fileName);
			e.printStackTrace();
		} finally
		{
			if (fileReader != null)
			{
				fileReader.close();
			}
			if (bufferReader != null)
			{
				bufferReader.close();
			}
		}
		return true;
	}
	
	public String[] GetFieldsByColumn(int index)
	{
		if((index<0)||(index > ColumnCount))
			return null;
		String[] cells = new String[RowCount];
		for(int i = 0; i< RowCount; i++)
		{
			cells[i] = Fields[i][index];
		}
		return cells;
	}
	
}
