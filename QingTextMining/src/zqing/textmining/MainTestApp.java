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
			cfg.ParseArgs(args); // ���������������������Ϣ����Configuration�����С�
			TextMining txtMining = new TextMining();

			CSVReader csvReader = new CSVReader();
			boolean bResult = csvReader.LoadFromFile(cfg.SourceFileName);
			DebugLog.Log(bResult ? String.format("Load %s succeed.", cfg.SourceFileName) :
				String.format("Load %s failed.", cfg.SourceFileName) );

			String[] strArraySrcLines = csvReader.GetFieldsByColumn(2); // ���CSV�ı��ĵڶ��е��ı����ݡ�
			strArraySrcLines = txtMining.ConnectBrokenLines(strArraySrcLines);
			String strConnectedTxt = txtMining.GetConnectedString(strArraySrcLines);
			
			//�ø������Դ�����еĶϾ������жϾ�
			Sentenizer.addPuncs('\n');
			String[] txtLines = Sentenizer.split(strConnectedTxt);
			DebugLog.Log("�Ͼ����.");
			
			// ����Ͼ�����TextLines.txt
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(cfg.ResultFolder + "/TextLines.txt", txtLines);
			DebugLog.Log(String.format("����Ͼ����� %s��ɡ�", cfg.ResultFolder + "/TextLines.txt"));
			
			// �ø������Դ�����ȶ������ı����зִʣ���������Ƶͳ��
			CWSTagger tag = new CWSTagger("./models/seg.m");
			String[] words = tag.tag2Array(strConnectedTxt);
			TreeMap<String, WordEntity> wordsMap = new TreeMap<String, WordEntity>();
			DebugLog.Log("CWSTagger��ȫ�ķִ���ɡ�");
			//��Ƶͳ��
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
			DebugLog.Log("��Ƶͳ����ɡ�");

			//��ͳ�ƵĴ�д��Words�ļ���
			Set<String> wordsSet = wordsMap.keySet();
			
			String[] wordsArray = wordsSet.toArray(new String[0]);
			csvExport.ExportLines(cfg.ResultFolder + "/words", wordsArray);
			DebugLog.Log(String.format("����ʵ䵽%s��ɡ�", cfg.ResultFolder + "/words"));
			
			// Ȼ��ֿ���ÿ���ı����зִ�
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
