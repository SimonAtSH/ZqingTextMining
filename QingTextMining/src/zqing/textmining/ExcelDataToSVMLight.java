package zqing.textmining;

import java.util.ArrayList;
import zqing.textmining.config.Configuration;
import zqing.textmining.input.ExcelReader;
import zqing.textmining.output.DebugLog;
import zqing.textmining.output.ExcelExporter;

public class ExcelDataToSVMLight
{

	public static void main(String[] args)
	{
		try
		{
			Configuration cfg = Configuration.getInstance();
			if (!cfg.ParseArgs(args))
				return; // 解析输入参数，将参数信息放入Configuration单例中。
			TextMining txtMining = new TextMining();
			ExcelReader excelReader = new ExcelReader(cfg.SourceExcelFileName);

			// 加载Ecel数据到String二维数组
			String[] strMotionValues = excelReader.GetFieldsByColumn(0);
			String[] strTextLines = excelReader.GetFieldsByColumn(1);
			// 对读入数据进行预处理
			for (int i = 0; i < strTextLines.length; i++)
				strTextLines[i] = strTextLines[i].replaceAll("\n", ""); // 主要是用来去掉网址后面跟的一个\n

			// 连接所有文本，用来分词，并生成词典。
			StringBuilder sbText = new StringBuilder();
			for (int i = 0; i < strTextLines.length; i++)
			{
				sbText.append(strTextLines[i]);
			}
			String strConnectedTxt = sbText.toString();
			
			//分词并进行词性标注，生成并输出词典到WordsTextFileName文件中
			txtMining.GenerateWordsDict(strConnectedTxt, cfg.WordsTextFileName);
			
			//生成SVM数据
			txtMining.GenerateSVMData(strTextLines, strMotionValues, cfg.AllSVMFileName,cfg.TrainSVMFileName, cfg.TestSVMFileName);


			// 使用Stanford parser对分词后的语句建立依赖树关系。
			String[] strDepTrees = null;
			String[] strDotLines = null;
			if (cfg.GenerateDependencyTree == true)
			{
				//TreePrintType 是用来决定生成何种依赖树，可以用 penn, 或者 typedDependencies
				ArrayList<String[]> results = txtMining.GenerateDepTreeAndDotLins(strTextLines, "typedDependencies", cfg.DependencyTreeFileName);
				strDepTrees = results.get(0);
				strDotLines = results.get(1);
			}

			// 句子分词，匹配极性词典，生成极性BaseLine
			int[] baseMotions = txtMining.GenerateMotionBaseLine(strTextLines);			
			int[] nsubjCounts = txtMining.GetDepRelationCount(strDepTrees, new String[]{"nsubj"});
			int[] dobjCounts = txtMining.GetDepRelationCount(strDepTrees, new String[]{"dobj"});
			int[] nsubjAnddobjCounts = txtMining.GetDepRelationCount(strDepTrees, new String[]{"nsubj","dobj"});
			

//			// 输出结果到excel中
			ExcelExporter xls = new ExcelExporter(cfg.DependencyTreeFileName);
			xls.ExportColumn(strMotionValues, 0, "Motion");
			xls.ExportColumn(strTextLines, 1, "Text");
			xls.ExportColumn(strDepTrees, 2, "Dependenc");
			xls.ExportColumn(strDotLines, 3, "Graphviz");
			xls.ExportColumn(baseMotions, 4, "BaseLine");
			xls.ExportColumn(nsubjCounts, 5, "nsubj");
			xls.ExportColumn(dobjCounts, 6, "dobj");
			xls.ExportColumn(nsubjAnddobjCounts, 7, "nsubj&dobj");
			xls.Close();
			DebugLog.Log(String.format("输出依赖树分析结果到 %s完成。", cfg.DependencyTreeFileName));	
			DebugLog.Log("数据分析完毕。");

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}



}
