package zqing.textmining.input;

import java.io.File;
import java.io.IOException;

import zqing.textmining.output.DebugLog;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelReader extends BaseReader
{
	private Workbook	book		= null;
	private Sheet		sheet		= null;
	private int ColumnCount = 0;
	private int RowCount = 0;
	
	public ExcelReader(String fileName)
	{
		try
		{
			DebugLog.Log(String.format("Loading %s", fileName));
			book = Workbook.getWorkbook(new File(fileName));
			sheet = book.getSheet(0);
			ColumnCount = sheet.getColumns();
			RowCount = sheet.getRows();
			DebugLog.Log(String.format("Load %s finished, %d Columns, %d Rows", fileName, ColumnCount, RowCount));
		} catch (IOException | BiffException e)
		{
			e.printStackTrace();
		}
	}
	
	public String[] GetFieldsByColumn(int iCol)
	{
		if((iCol<0)||(iCol > ColumnCount))
			return null;
		String[] cells = new String[RowCount];
		for(int iRow = 0; iRow< RowCount; iRow++)
		{
			Cell c = sheet.getCell(iCol, iRow);
			if(c!=null )
				cells[iRow] = c.getContents();
		}
		return cells;		
	}
	
	public String[][] GetFieldsBySheet(int iPage)
	{
		String[][] cells = null;
		sheet = book.getSheet(iPage);
		if(sheet != null)
		{
			ColumnCount = sheet.getColumns();
			RowCount = sheet.getRows();
			cells = new String[RowCount][ColumnCount];
			for(int iRow = 0; iRow < RowCount; iRow++)
			{
				for(int iCol = 0; iCol < ColumnCount; iCol++)
				{
					Cell c = sheet.getCell(iCol,  iRow);
					if(c!=null)
						cells[iRow][iCol] = c.getContents();
				}
			}
		}
		return cells;		
	}
	
	public String GetField(int iRow, int iCol)
	{
		Cell c = sheet.getCell(iCol, iRow);
		if(c!=null)
			return c.getContents();
		return null;
	}

	public boolean Close()
	{
		try
		{
			book.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

}
