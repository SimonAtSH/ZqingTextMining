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
			cfg.ParseArgs(args); // ���������������������Ϣ����Configuration�����С�
			TextMining txtMining = new TextMining();
			ExcelReader excelReader = new ExcelReader(cfg.SourceExcelFileName);

			// ���Excel�ĵڶ��е��ı����ݡ�
			String[] strArraySrcLines = excelReader.GetFieldsByColumn(1); 
			for(int i=0; i<strArraySrcLines.length; i++)
				strArraySrcLines[i] = strArraySrcLines[i].replaceAll("\n", ""); //��Ҫ������ȥ����ַ�������һ��\n
			String strConnectedTxt = txtMining.GetConnectedString(strArraySrcLines);
			
			DebugLog.Log("Initiating CWSTagger");
			CWSTagger cwsTag = new CWSTagger("./models/seg.m", new edu.fudan.ml.types.Dictionary("./models/dict.txt"));
			// Boolֵָ����dict�Ƿ�����cws�ִʣ��ִʺʹ��Կ���ʹ�ò�ͬ�Ĵʵ䣩// true���滻��֮ǰ��dict.txt
			DebugLog.Log("Initiating POSTagger");
			POSTagger ptag = new POSTagger(cwsTag, "models/pos.m", new edu.fudan.ml.types.Dictionary("./models/dict.txt"), true); 
			ptag.removeDictionary(false);// ���Ƴ��ִʵĴʵ�
			ptag.setDictionary(new edu.fudan.ml.types.Dictionary("./models/dict.txt"), false);// ����POS�ʵ䣬�ִ�ʹ��ԭ������

			//��ȫ�ķִʲ���ע����
			DebugLog.Log("POSTagger��ʼ��ȫ�Ľ��зִʺʹ��Ա�ע��");
			String[][] wordsAndPos = ptag.tag2Array(strConnectedTxt);
			String[] words = wordsAndPos[0];
			
			//��Ӹ��Ӵ��ԣ����ŵ�
			//DebugLog.Log("��Ӹ��Ӵ��ԣ����ţ�������ŵȡ�");			
			//words = txtMining.AddAddiontalWords(words);

			// ���ɴʵ�
			DebugLog.Log("��ʼ���ɴʵ䡣");			
			TreeMap<String, WordEntity> wordsDict = new TreeMap<String, WordEntity>();
			// ͳ�Ƹ����ʵĴ�Ƶ
			wordsDict = txtMining.GetWordsDict(words, wordsDict);
			// ͳ�Ƹ������Ե�����
			wordsDict = txtMining.GetWordsDict(txtMining.AddtionalWords, wordsDict, 0);
			wordsDict = txtMining.GetWordsDict(wordsAndPos[1], wordsDict, 1);
			DebugLog.Log("�ʵ�������ϡ�");

			Set<String> wordsSet = wordsDict.keySet();			
			String[] wordsArray = wordsSet.toArray(new String[0]);

			// ��ͳ�ƵĴ�д��Words�ļ���
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(cfg.ResultFolder + "/words.txt", wordsArray);
			DebugLog.Log(String.format("����ʵ䵽%s��ɡ�", cfg.ResultFolder + "/words.txt"));
			
			// ����ôʵ��ļ����趨wordsDict��ÿ��key��Ӧ�ڴʵ������id��
			int index = 0;
			for(String s: wordsArray)
			{
				WordEntity w = wordsDict.get(s);
				w.Index = index++;
			}
			
			DebugLog.Log("��ʼ����SVM���ݡ�");
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
				// ͳ�ƴ�Ƶ
				wordsMap = txtMining.GetWordsDict(wps[0], wordsMap);
				// ͳ�ƴ��Գ��ִ���
				wordsMap = txtMining.GetWordsDict(wps[1], wordsMap);
				
				String svm = txtMining.GenerateSVMLine(motion,wordsMap, wordsDict);
				svmLines.add(svm);
			}
			
			csvExport.ExportLines(cfg.ResultFolder + "/train.txt", svmLines.toArray(new String[0]));
			DebugLog.Log(String.format("����SVM����%s���", cfg.ResultFolder + "/train.txt"));

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
