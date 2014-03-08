package zqing.textmining;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.fudan.nlp.cn.Sentenizer;
import edu.fudan.nlp.cn.tag.CWSTagger;
import zqing.textmining.config.Configuration;
import zqing.textmining.entity.WordEntity;
import zqing.textmining.input.CSVReader;
import zqing.textmining.output.ExcelExporter;
import zqing.textmining.output.TextExporter;

public class MainTestApp
{
	public static void main(String[] args)
	{
		try
		{
			Configuration cfg = Configuration.getInstance();
			cfg.ParseArgs(args); // 解析输入参数，将参数信息放入Configuration单例中。
			TextMining txtMining = new TextMining();

			CSVReader csvReader = new CSVReader();
			csvReader.LoadFromFile(cfg.SourceFileName);

			String[] txtSrcLines = csvReader.GetFieldsByColumn(2); // 获得CSV文本的第二列的文本数据。
			String[] txtConnectedLines = txtMining.ConnectBrokenLines(txtSrcLines);
			String txtSrc = txtMining.GetConnectedString(txtConnectedLines);
			
			//用复旦语言处理库中的断句的类进行断句
			Sentenizer.addPuncs('\n');
			String[] txtLines = Sentenizer.split(txtSrc);

//			//结果用文本输出类写到文本文件中
//			TextExporter txtExporter = new TextExporter();
//			txtExporter.Export(cfg.ResultFileName, txtLines);
			
			//结果用文本输出类写到Excel文件中
			ExcelExporter excelExp = new ExcelExporter(cfg.ResultFileName);
			excelExp.ExportLines(txtLines);
			
			// 用复旦语言处理库对每句进行分词
			CWSTagger tag = new CWSTagger("./models/seg.m");
			excelExp.AddSheet();
			HashMap<String, WordEntity> WordsDict = new HashMap<String, WordEntity>();
			int iRow = 0;
			for(String s:txtConnectedLines)
			{	
				if(s.trim().isEmpty())
					continue;
				String[] words = tag.tag2Array(s);
				excelExp.ExportFieldsInOneRow(words, iRow);
				iRow++;

				//词频统计
				for(String w:words)
				{
					if(!(w.trim().isEmpty()))
					{
						if(WordsDict.containsKey(w))
						{
							WordEntity wEntity = WordsDict.get(w);
							wEntity.Count++;
						}
						else
						{
							WordsDict.put(w, new WordEntity(w,1));
						}						
					}					
				}
			}
			
			// 输出词频统计结果
			excelExp.AddSheet();
			Iterator<Entry<String, WordEntity>> iter = WordsDict.entrySet().iterator();
			iRow = 0;
			while (iter.hasNext()) 
			{
				Entry<String, WordEntity> entry = iter.next();
				String[] results = new String[2]{entry.getKey(),  (entry.getValue().Count) };
				//Label l = entry.getKey();
			Object val = entry.getValue();
			}
			 
			excelExp.Close();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
