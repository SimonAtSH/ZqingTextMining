package zqing.textmining;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;
import zqing.textmining.config.Configuration;
import zqing.textmining.entity.WordEntity;
import zqing.textmining.input.ExcelReader;
import zqing.textmining.output.CSVExporter;
import zqing.textmining.output.DebugLog;

public class ExcelDataToSVMLight
{

	
	public static void main(String[] args)
	{
		try
		{
			Configuration cfg = Configuration.getInstance();
			cfg.ParseArgs(args); // 解析输入参数，将参数信息放入Configuration单例中。
			TextMining txtMining = new TextMining();
			ExcelReader excelReader = new ExcelReader(cfg.SourceExcelFileName);

			// 获得Excel的第二列的文本数据。
			String[] strArraySrcLines = excelReader.GetFieldsByColumn(1); 
			for(int i=0; i<strArraySrcLines.length; i++)
				strArraySrcLines[i] = strArraySrcLines[i].replaceAll("\n", ""); //主要是用来去掉网址后面跟的一个\n
			String strConnectedTxt = txtMining.GetConnectedString(strArraySrcLines);
			
			DebugLog.Log("Initiating CWSTagger");
			CWSTagger cwsTag = new CWSTagger("./models/seg.m", new edu.fudan.ml.types.Dictionary("./models/dict.txt"));
			// Bool值指定该dict是否用于cws分词（分词和词性可以使用不同的词典）// true就替换了之前的dict.txt
			DebugLog.Log("Initiating POSTagger");
			POSTagger ptag = new POSTagger(cwsTag, "models/pos.m", new edu.fudan.ml.types.Dictionary("./models/dict.txt"), true); 
			ptag.removeDictionary(false);// 不移除分词的词典
			ptag.setDictionary(new edu.fudan.ml.types.Dictionary("./models/dict.txt"), false);// 设置POS词典，分词使用原来设置

			//对全文分词并标注词性
			DebugLog.Log("POSTagger开始对全文进行分词和词性标注。");
			String[][] wordsAndPos = ptag.tag2Array(strConnectedTxt);
			String[] words = wordsAndPos[0];
			
			//添加附加词性，符号等
			//DebugLog.Log("添加附加词性，符号，特殊符号等。");			
			//words = txtMining.AddAddiontalWords(words);

			// 生成词典
			DebugLog.Log("开始生成词典。");			
			TreeMap<String, WordEntity> wordsDict = new TreeMap<String, WordEntity>();
			// 统计各个词的词频
			wordsDict = txtMining.GetWordsDict(words, wordsDict);
			// 统计各个词性的数量
			wordsDict = txtMining.GetWordsDict(txtMining.AddtionalWords, wordsDict, 0);
			wordsDict = txtMining.GetWordsDict(wordsAndPos[1], wordsDict, 1);
			DebugLog.Log("词典生成完毕。");

			Set<String> wordsSet = wordsDict.keySet();			
			String[] wordsArray = wordsSet.toArray(new String[0]);

			// 将统计的词写入Words文件中
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(cfg.ResultFolder + "/words.txt", wordsArray);
			DebugLog.Log(String.format("输出词典到%s完成。", cfg.ResultFolder + "/words.txt"));
			
			// 输出好词典文件后，设定wordsDict中每个key对应于词典的索引id。
			int index = 0;
			for(String s: wordsArray)
			{
				WordEntity w = wordsDict.get(s);
				w.Index = index++;
			}
			
			DebugLog.Log("开始生成SVM数据。");
			String[][] signedLines = excelReader.GetFieldsBySheet(0);
			ArrayList<String> svmLines = new ArrayList<String>();
			TreeMap<String, WordEntity> wordsMap = new TreeMap<String, WordEntity>();
			String motion = "0";
			for(int i=0; i < strArraySrcLines.length; i++)
			{
				motion = signedLines[i][0];				
				String[][] wps = ptag.tag2Array(signedLines[i][1]);
				if(wps == null)
					continue;
				wordsMap.clear();
				// 统计词频
				wordsMap = txtMining.GetWordsDict(wps[0], wordsMap);
				// 统计词性出现次数
				wordsMap = txtMining.GetWordsDict(wps[1], wordsMap);
				
				String svm = txtMining.GenerateSVMLine(motion,wordsMap, wordsDict);
				svmLines.add(svm);
			}
			
			csvExport.ExportLines(cfg.ResultFolder + "/train.txt", svmLines.toArray(new String[0]));
			DebugLog.Log(String.format("导出SVM数据%s完毕", cfg.ResultFolder + "/train.txt"));

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
