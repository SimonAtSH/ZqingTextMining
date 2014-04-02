package zqing.textmining.output;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelExporter extends BaseExporter
{
	private WritableWorkbook	book		= null;
	private WritableSheet		sheet		= null;

	public ExcelExporter(String fileName)
	{
		try
		{
			book = Workbook.createWorkbook(new File(fileName));
			String[] sheets = book.getSheetNames();
			int sheetNum = sheets.length;			
			sheet = book.createSheet("Page" + (sheetNum++) , 0);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean AddSheet()
	{
		if(book !=null)
		{
			String[] sheets = book.getSheetNames();
			int sheetNum = sheets.length;			
			sheet = book.createSheet("Page" + (sheetNum++) , 0);
			return true;
		}
		else
		{
			return false;
		}
	}

	public long ExportLines(String[] Lines)
	{
		try
		{
			int i = 0;
			for (String s : Lines)
			{
				Label label = new Label(0, i, s);
				sheet.addCell(label);
				i++;
			}
			// 写入数据
			book.write();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (RowsExceededException e)
		{
			e.printStackTrace();
		} catch (WriteException e)
		{
			e.printStackTrace();
		}

		return -1;
	}

	public boolean Close()
	{
		try
		{
			book.write();
			book.close();
		} catch (WriteException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}

	public boolean ExportFieldsInOneRow( String[] words, int iRow)
	{
		if ((book == null) || (sheet == null))
		{
			return false;
		}

		try
		{
			int iCol = 0;
			for (String s : words)
			{
				Label label = new Label(iCol, iRow, s);
				sheet.addCell(label);
				iCol++;
			}
			book.write();
		} catch (RowsExceededException e)
		{
			e.printStackTrace();
		} catch (WriteException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean ExportFields( String[][] words)
	{
		if ((book == null) || (sheet == null))
		{
			return false;
		}

		try
		{
			for(int iRow = 0; iRow < words.length; iRow++)
				for(int iCol = 0; iCol < words[iRow].length; iCol++)
				{
					Label label = new Label(iCol, iRow, words[iRow][iCol]);
					sheet.addCell(label);
				}
			book.write();
		} catch (RowsExceededException e)
		{
			e.printStackTrace();
		} catch (WriteException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}

}
