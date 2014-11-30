package zqing.textmining;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.fudan.nlp.cn.Sentenizer;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import zqing.textmining.config.Configuration;
import zqing.textmining.entity.LineEntity;
import zqing.textmining.input.TextReader;
import zqing.textmining.output.DebugLog;

public class BookFileProcess
{
	private TextMining	txtMining;
	public BookFileProcess()
	{
		Sentenizer.addPuncs('.');
		Sentenizer.addPuncs('?');
		Sentenizer.addPuncs('!');
		Sentenizer.addPuncs('。');
		Sentenizer.addPuncs('？');
		Sentenizer.addPuncs('！');		
	}


	
	public ArrayList<LineEntity> ToLineEntityList(String src)
	{
		String[] strs = Sentenizer.split(src);
		return ToLineEntityList(strs);
	}
	
	public ArrayList<LineEntity> ToLineEntityList(String[] lines)
	{
		ArrayList<LineEntity> resultList = new ArrayList<LineEntity>();
		//DebugLog.Log("文本行数:"+ lines.length);
		//int index = 0;
		for(int i = 0; i < lines.length; i++)
		{
			//if(lines[i].length() <= 1) continue;
			LineEntity line = new LineEntity(lines[i]);
			line.Index = i;
			resultList.add(line);
		}
		return resultList;
	}
	
	/**
	 * 读入文本文件，按换行分解为段落，然后再分解每个段落为句子，结果输出到ArrayList<LineEntity>树型结构中
	 * @param fileName
	 * @param motion 情感值，如 1或者-1.
	 * @return
	 */
	public ArrayList<LineEntity> ProcessFile(String fileName, double motion)
	{
		ArrayList<LineEntity> lines = null;
		try
		{
			DebugLog.Log("开始处理:"+fileName);
			//读入文本，按照换行分解为段落。
			TextReader txtReader = new TextReader();
			String[] strings = txtReader.LoadFromFile(fileName);
			lines = ToLineEntityList(strings);		
			txtMining = new TextMining();
			txtMining.InitTagger();
			txtMining.InitDepTreeParser("typedDependencies");
			for(LineEntity line : lines)
			{
				line.Motion = motion;
				//分解每个段落，以“。”，“?”,”!”作为分句标识，得到多个句子。
				line.SubLines = this.ToLineEntityList(line.Text);
				DebugLog.Log("分解段落:"+ line.Index + " 句子数量:"+ line.SubLines.size());
				for(LineEntity subLine: line.SubLines)
				{
					subLine.DepTree = txtMining.GetDepTree(subLine.Text);
				}
			}
			DebugLog.Log("完成处理:"+ fileName);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return lines;
	}
	
	public void ExportLines(ArrayList<LineEntity> lines, String fileName)
	{
		try
		{
			DebugLog.Log("开始输出Excel:"+ fileName);
			// 输出结果到excel中
			WritableWorkbook	book		= null;
			WritableSheet		sheet		= null;
			book = Workbook.createWorkbook(new File(fileName));
			sheet = book.createSheet("Page1", 0);
			int row = 1;
			for(int i = 0; i< lines.size(); i++)
			{
				LineEntity line = lines.get(i);

				for(int j = 0; j < line.SubLines.size(); j++)
				{
					LineEntity subLine = line.SubLines.get(j);
					sheet.addCell(new Label(0, row, String.valueOf(line.Motion)));
					sheet.addCell(new Label(1, row, String.valueOf(i)));
					sheet.addCell(new Label(2, row, line.Text));	
					sheet.addCell(new Label(3, row, String.valueOf(subLine.Index)));
					sheet.addCell(new Label(4, row, subLine.Text));	
					sheet.addCell(new Label(5, row, subLine.DepTree));	
					row++;
				}
			}                                                                                            
			book.write();
			book.close();
			DebugLog.Log("完成输出Excel："+ fileName);

		} catch (IOException e)
		{
			e.printStackTrace();
		}  catch (WriteException e)
		{
			e.printStackTrace();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args)
	{
		try
		{
			Configuration cfg = Configuration.getInstance();
			if(!cfg.ParseArgs(args))
				return; // 解析输入参数，将参数信息放入Configuration单例中。
			
			BookFileProcess process = new BookFileProcess();
//			ArrayList<LineEntity> posLines = process.ProcessFile("C:\\works\\simon\\workspace\\QingTextMining\\exampleData\\BookFileProcess\\Test.txt", 1.0);
//			process.ExportLines(posLines, "C:\\works\\simon\\workspace\\QingTextMining\\exampleData\\BookFileProcess\\Test.txt.xls");
			
//			ArrayList<LineEntity> posLines = process.ProcessFile(cfg.SourcePosTextFileName, 1.0);
//			process.ExportLines(posLines, cfg.SourcePosTextFileName + ".xls");
			ArrayList<LineEntity> negLines = process.ProcessFile(cfg.SourceNegTextFileName,  -1.0);
			process.ExportLines(negLines, cfg.SourceNegTextFileName + ".xls");
			DebugLog.Log("BookFileProcess 完成");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
