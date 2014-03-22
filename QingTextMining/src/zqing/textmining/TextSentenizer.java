package zqing.textmining;

import edu.fudan.nlp.cn.Sentenizer;
import zqing.textmining.config.Configuration;
import zqing.textmining.input.CSVReader;
import zqing.textmining.output.CSVExporter;
import zqing.textmining.output.DebugLog;

public class TextSentenizer
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

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
