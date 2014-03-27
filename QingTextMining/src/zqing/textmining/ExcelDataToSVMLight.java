package zqing.textmining;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
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
			if(!cfg.ParseArgs(args))
				return; // 解析输入参数，将参数信息放入Configuration单例中。
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
			POSTagger ptag = new POSTagger(cwsTag, "./models/pos.m", new edu.fudan.ml.types.Dictionary("./models/dict.txt"), true); 
			ptag.removeDictionary(false);// 不移除分词的词典
			ptag.setDictionary(new edu.fudan.ml.types.Dictionary("./models/dict.txt"), false);// 设置POS词典，分词使用原来设置

			//对全文分词并标注词性
			DebugLog.Log("POSTagger开始对全文进行分词和词性标注。");
			String[][] wordsAndPos = ptag.tag2Array(strConnectedTxt);			
			
			//添加附加词性，符号等
			//DebugLog.Log("添加附加词性，符号，特殊符号等。");			
			//words = txtMining.AddAddiontalWords(words);

			// 生成词典
			DebugLog.Log("开始生成词典。");			
			TreeMap<String, WordEntity> wordsDict = new TreeMap<String, WordEntity>();
			wordsDict = txtMining.GetWordsDict(wordsAndPos[0], wordsDict);

			// 生成词性的词典
			TreeMap<String, WordEntity> posDict = new TreeMap<String, WordEntity>();
			posDict = txtMining.GetWordsDict(txtMining.AddtionalWords, posDict, 0);
			posDict = txtMining.GetWordsDict(wordsAndPos[1], posDict, 1);
			DebugLog.Log("词典生成完毕。");

			// 将词典写入Words.txt文件中
			Set<String> wordsSet = wordsDict.keySet();			
			String[] wordsArray = wordsSet.toArray(new String[0]);
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(cfg.ResultFolder + "/words.txt", wordsArray);
			
			//将词性也写入Words.txt文件中。
			Set<String> posSet = posDict.keySet();
			String[] posArray = posSet.toArray(new String[0]);
			csvExport.ExportLines(cfg.ResultFolder + "/words.txt", posArray, true); //Append的方式追加。
			
			DebugLog.Log(String.format("输出词典到%s完成。", cfg.ResultFolder + "/words.txt"));
			
			// 输出好词典文件后，设定wordsDict中每个key对应于词典的索引id。
			int index = 0;
			for(String s: wordsArray)
			{
				WordEntity w = wordsDict.get(s);
				w.Index = index++;
			}
			for(String s: posArray)
			{
				WordEntity w = posDict.get(s);
				w.Index = index++;
			}			
			
			DebugLog.Log("开始生成SVM数据。");
			String[][] signedLines = excelReader.GetFieldsBySheet(0);
			ArrayList<String> svmLines = new ArrayList<String>();
			TreeMap<String, WordEntity> wordsMap = new TreeMap<String, WordEntity>();
			TreeMap<String, WordEntity> posMap = new TreeMap<String, WordEntity>();
			String motion = "0";
			for(int i=0; i < strArraySrcLines.length; i++)
			{
				motion = signedLines[i][0];				
				String[][] wps = ptag.tag2Array(signedLines[i][1]);
				if(wps == null)
					continue;
				// 统计词频
				wordsMap.clear();				
				wordsMap = txtMining.GetWordsDict(wps[0], wordsMap);
				
				// 统计词性出现次数
				posMap.clear();
				posMap = txtMining.GetWordsDict(wps[1], posMap);

				// 加上字数统计
				posMap.put("字数",  new WordEntity("字数",signedLines[i][1].length()));
				
				String svm = txtMining.GenerateSVMLine(motion,wordsMap, posMap, wordsDict, posDict);
				svmLines.add(svm);
			}
			
			csvExport.ExportLines(cfg.ResultFolder + "/train.txt", svmLines.toArray(new String[0]));
			DebugLog.Log(String.format("导出SVM数据%s完毕", cfg.ResultFolder + "/train.txt"));

			//使用Stanford parser对分词后的语句建立依赖树关系。
			LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/chinesePCFG.ser.gz");
			for(int i=0; i<strArraySrcLines.length; i++)
			{
				String[][] wps = ptag.tag2Array(signedLines[i][1]);
				List<CoreLabel> rawWords = Sentence.toCoreLabelList(wps[0]);
				Tree parse = lp.apply(rawWords);
				parse.pennPrint();
				DebugLog.Log("------------------------------");
			}
			DebugLog.Log("依赖树分析完毕。");

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
