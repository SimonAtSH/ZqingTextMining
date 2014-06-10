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
			if(!cfg.ParseArgs(args))
				return; // 解析输入参数，将参数信息放入Configuration单例中。
			TextMining txtMining = new TextMining();

			CSVReader csvReader = new CSVReader();
			boolean bResult = csvReader.LoadFromFile(cfg.SourceTextFileName);
			DebugLog.Log(bResult ? String.format("Load %s succeed.", cfg.SourceTextFileName) :
				String.format("Load %s failed.", cfg.SourceTextFileName) );

			String[] strArraySrcLines = csvReader.GetFieldsByColumn(0); // 获得CSV文本的第0列的文本数据。
			strArraySrcLines = txtMining.ConnectBrokenLines(strArraySrcLines);
			String strConnectedTxt = txtMining.GetConnectedString(strArraySrcLines);
			
			String[] ignorePrefix = { "一", "二", "三", "四", "五", "六", "七", "八", "九", 
					"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", 
					"１", "２", "３", "４", "５", "６", "７", "８", "９", "０", "\""};
			
			// 用复旦语言处理库中的断句的类进行断句
			// 复旦语言处理库中缺省用这几个符号断句：
			// puncs	= new char[] { '。', '？', '！', '；' };
			Sentenizer.addPuncs('\n');
			Sentenizer.addPuncs('.');
			Sentenizer.addPuncs(';');
			String[] txtLines = Sentenizer.split(strConnectedTxt);
			DebugLog.Log("断句完成.");			
			// 输出断句结果到TextLines.txt
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(cfg.TextLinesFileName , txtLines, false, 5, ignorePrefix);
			DebugLog.Log(String.format("输出断句结果到 %s完成。", cfg.TextLinesFileName));
			
			Sentenizer.addPuncs(',');
			Sentenizer.addPuncs('，');
			Sentenizer.addPuncs(' ');			
			Sentenizer.addPuncs(':');			
			Sentenizer.addPuncs('：');			
			Sentenizer.addPuncs('（');			
			Sentenizer.addPuncs('）');			
			Sentenizer.addPuncs('(');			
			Sentenizer.addPuncs(')');			
			txtLines = Sentenizer.split(strConnectedTxt);
			DebugLog.Log("分句断句完成.");
			csvExport.ExportLines(cfg.SubTextLinesFileName, txtLines, false, 5, ignorePrefix);			
			DebugLog.Log(String.format("输出断句结果到 %s完成。", cfg.SubTextLinesFileName));

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
