package zqing.textmining;

import zqing.textmining.config.Configuration;
import zqing.textmining.input.CSVReader;
import zqing.textmining.output.DebugLog;

public class FeatureExtract
{

	public FeatureExtract()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)
	{
		try
		{
			Configuration cfg = Configuration.getInstance();
			if(!cfg.ParseArgs(args))
				return; // 解析输入参数，将参数信息放入Configuration单例中。
			TextMining txtMining = new TextMining();
			
			// 读入文本数据
			CSVReader csvReader = new CSVReader();
			boolean bResult = csvReader.LoadFromFile(cfg.SourceTextFileName);
			DebugLog.Log(bResult ? String.format("Load %s succeed.", cfg.SourceTextFileName) :
				String.format("Load %s failed.", cfg.SourceTextFileName) );

			String[] strArraySrcLines = csvReader.GetFieldsByColumn(2); // 获得CSV文本的第3列的文本数据。
			strArraySrcLines = txtMining.ConnectBrokenLines(strArraySrcLines);
			String strConnectedTxt = txtMining.GetConnectedString(strArraySrcLines);
			
			//特征提取 F1，生成一元词词典
			//分词并进行词性标注，生成并输出词典到WordsTextFileName文件中
			//txtMining.GenerateUnigramWordsDict(strConnectedTxt, cfg.WordsTextFileName);
			
			//特征提取 F2， 生成二元词词典
			txtMining.GenerateBigramWordsDict(strConnectedTxt, cfg.BigramWordsTextFileName);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

}
