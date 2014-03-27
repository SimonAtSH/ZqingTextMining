package zqing.textmining.output;

public class BaseExporter
{
	public BaseExporter()
	{	
	}
	
	public long Export(Object obj)
	{
		ExportHeader(obj);
		ExportBody(obj);
		ExportFooter(obj);		
		return -1;
	}
	
	private long ExportHeader(Object obj)
	{
		return -1;
	}
	
	private long ExportBody(Object obj)
	{
		return -1;
	}
	
	private long ExportFooter(Object obj)
	{
		return -1;
	}
	
//	public void ExportStringMatrix(String[][] WordsMatrix)
//	{
//		for (int i = 0; i < WordsMatrix.length; i++)
//		{
//			for (int j = 0; j < WordsMatrix[i].length; j++)
//			{
//				System.out.print(WordsMatrix[i][j] + " | ");
//			}
//			System.out.println();
//		}
//	}	
//	
//	public void ExportStrings(String[] lines)
//	{
//		for(String s:lines)
//		{
//			System.out.println(s);
//		}
//	}
}
