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

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
