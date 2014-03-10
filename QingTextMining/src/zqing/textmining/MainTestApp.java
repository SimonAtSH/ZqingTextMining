package zqing.textmining;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.fudan.nlp.cn.Sentenizer;
import edu.fudan.nlp.cn.tag.CWSTagger;
import zqing.textmining.config.Configuration;
import zqing.textmining.entity.WordEntity;
import zqing.textmining.input.CSVReader;
import zqing.textmining.output.CSVExporter;
import zqing.textmining.output.DebugLog;

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
			boolean bResult = csvReader.LoadFromFile(cfg.SourceFileName);
			DebugLog.Log(bResult ? String.format("Load %s succeed.", cfg.SourceFileName) :
				String.format("Load %s failed.", cfg.SourceFileName) );

			String[] strArraySrcLines = csvReader.GetFieldsByColumn(2); // 获得CSV文本的第二列的文本数据。
			strArraySrcLines = txtMining.ConnectBrokenLines(strArraySrcLines);
			String strConnectedTxt = txtMining.GetConnectedString(strArraySrcLines);
			
			//用复旦语言处理库中的断句的类进行断句
			Sentenizer.addPuncs('\n');
			String[] txtLines = Sentenizer.split(strConnectedTxt);
			DebugLog.Log("断句完成.");
			
			// 输出断句结果到TextLines.txt
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(cfg.ResultFolder + "/TextLines.txt", txtLines);
			DebugLog.Log(String.format("输出断句结果到 %s完成。", cfg.ResultFolder + "/TextLines.txt"));
			
			// 用复旦语言处理库先对整个文本进行分词，用来做词频统计
			CWSTagger tag = new CWSTagger("./models/seg.m");
			String[] words = tag.tag2Array(strConnectedTxt);
			TreeMap<String, WordEntity> wordsMap = new TreeMap<String, WordEntity>();
			DebugLog.Log("CWSTagger对全文分词完成。");
			//词频统计
			for(String w:words)
			{
				if(!(w.trim().isEmpty()))
				{
					if(wordsMap.containsKey(w))
					{
						WordEntity wEntity = wordsMap.get(w);
						wEntity.Count++;
					}
					else
					{
						wordsMap.put(w, new WordEntity(w,1));
					}						
				}					
			}
			DebugLog.Log("词频统计完成。");

			//将统计的词写入Words文件中
			Set<String> wordsSet = wordsMap.keySet();
			
			String[] wordsArray = wordsSet.toArray(new String[0]);
			csvExport.ExportLines(cfg.ResultFolder + "/words", wordsArray);
			DebugLog.Log(String.format("输出词典到%s完成。", cfg.ResultFolder + "/words"));
			
			// 然后分开对每行文本进行分词
			/*for(String s:strArraySrcLines)
			{	
				if(s.trim().isEmpty())	continue;
				String[] wordsOfLine = tag.tag2Array(s);
				for(String w:wordsOfLine)
				{
					//int index = 
				}
			}*/
			
			
//			for (String key : WordsDict.keySet().toArray()) {
//
//			    value = map.get(key);
//
//			}
			
			
//			excelExp.Close();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
