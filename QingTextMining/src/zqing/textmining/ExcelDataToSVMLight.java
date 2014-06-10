package zqing.textmining;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import zqing.textmining.config.Configuration;
import zqing.textmining.entity.WordEntity;
import zqing.textmining.input.ExcelReader;
import zqing.textmining.output.CSVExporter;
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
			String[][] strExcelSrcData = excelReader.GetFieldsBySheet(0);
			// 对读入数据进行预处理
			for (int i = 0; i < strExcelSrcData.length; i++)
				strExcelSrcData[i][1] = strExcelSrcData[i][1].replaceAll("\n", ""); // 主要是用来去掉网址后面跟的一个\n

			// 连接所有文本，用来分词，并生成词典。
			StringBuilder sbText = new StringBuilder();
			for (int i = 0; i < strExcelSrcData.length; i++)
			{
				sbText.append(strExcelSrcData[i][1]);
			}
			String strConnectedTxt = sbText.toString();
			
			//分词并进行词性标注，生成并输出词典到WordsTextFileName文件中
			txtMining.GenerateWordsDict(strConnectedTxt, cfg.WordsTextFileName);
			
			DebugLog.Log("开始生成SVM数据。");
			ArrayList<String> svmLines = new ArrayList<String>();
			ArrayList<String> trainList = new ArrayList<String>(); // Train 数据包含70%的SVM数据
			ArrayList<String> testList = new ArrayList<String>(); // Test 数据包含30%的SVM数据
			
			
			TreeMap<String, WordEntity> wordsMap = new TreeMap<String, WordEntity>(); // 用于对每句话进行分词后统计词频，生成统计结果
			TreeMap<String, WordEntity> posMap = new TreeMap<String, WordEntity>(); // 用于对每句话进行词性标注后统计词性数量
			String motion = "0"; // 句子情感极性
			int[] MotionCount = new int[3]; // 根据极性分别统计
			int[] testListFilter = { 3, 6, 9 }; // MotionCount[i]对10取模，然后取模结果在该数组中的话，该SVM语句就放在testList中。

			// 开始对每句话进行分词，词性标注，统计词频，统计极性，生成TrainList和TestList
			for (int i = 0; i < strExcelSrcData.length; i++)
			{
				String[][] wps = txtMining.posTagger.tag2Array(strExcelSrcData[i][1]); // 分词并做词性标注
				if (wps == null)
					continue;

				for (int j = 0; j < wps.length; j++)
				{
					// edu.fudan.nlp.cn.tag.CWSTagger分词后似乎会在网址后加上一个\n，所以这里要去掉它
					wps[j][0] = wps[j][0].replaceAll("\n", "");
				}

				// 统计词频
				wordsMap.clear();
				wordsMap = txtMining.GetWordsDict(wps[0], wordsMap);

				// 统计词性出现次数
				posMap.clear();
				posMap = txtMining.GetWordsDict(wps[1], posMap);

				// 字数统计
				posMap.put("字数", new WordEntity("字数", strExcelSrcData[i][1].length()));

				// 生成SVM数据
				motion = strExcelSrcData[i][0]; // 极性
				String svm = txtMining.GenerateSVMLine(motion, wordsMap, posMap, txtMining.wordsDict, txtMining.posDict);
				int iMo = Integer.parseInt(motion);
				if (iMo == -1 || iMo == 0 || iMo == 1)
				{
					MotionCount[iMo + 1]++; // iMo+1等于0,1,2。那么MotionCount[0]..[2]分别存放iMo为-1,0,1的统计结果
					int j = MotionCount[iMo + 1] % 10; // 对10取模
					Boolean bInTest = false;
					for (int k : testListFilter)
					{
						if (k == j)
						{
							bInTest = true;
							break;
						}
					}
					if (bInTest)
					{
						testList.add(svm);
					} else
					{
						trainList.add(svm);
					}
				} else
				{
					DebugLog.Log(String.format("第 %d 行数据异常，句子极性为 %d 。", i, iMo));
				}
				svmLines.add(svm);
			}
			
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(cfg.AllSVMFileName, svmLines.toArray(new String[0]));
			DebugLog.Log(String.format("导出SVM数据%s完毕", cfg.AllSVMFileName));
			csvExport.ExportLines(cfg.TrainSVMFileName, trainList.toArray(new String[0]));
			DebugLog.Log(String.format("导出SVM数据%s完毕", cfg.TrainSVMFileName));
			csvExport.ExportLines(cfg.TestSVMFileName, testList.toArray(new String[0]));
			DebugLog.Log(String.format("导出SVM数据%s完毕", cfg.TestSVMFileName));

			// 使用Stanford parser对分词后的语句建立依赖树关系。
			if (cfg.GenerateDependencyTree == true)
			{
				LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/chinesePCFG.ser.gz");
				//LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
				TreebankLanguagePack tlp = lp.getOp().langpack();
				//TreePrint tp = new TreePrint("typedDependencies", tlp);
				TreePrint tp = new TreePrint("penn", tlp);
				GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
				// 输出极性，语句，和依赖树,和Graphviz的DOT图形描述到strs中。
				String[][] strs = new String[strExcelSrcData.length][4];
				boolean bFindKeyWords = cfg.KeyDict.length > 0? true:false;
				boolean bHasKey = false;
				for (int i = 0; i < strExcelSrcData.length; i++)
				{
					strs[i][0] = strExcelSrcData[i][0];
					strs[i][1] = strExcelSrcData[i][1].replaceAll("\n", "");
					strs[i][2] = "";
					strs[i][3] = "";
										
					if(bFindKeyWords )
					{
						bHasKey = false;
						for (String k : cfg.KeyDict)
						{
							if (strs[i][1].indexOf(k) >= 0)
							{
								bHasKey = true;
								break;
							}
						}
					}
					else
						bHasKey = true;
					//DebugLog.Log(String.format("%d %s %s", i, strs[i][0], strs[i][1]));
					DebugLog.Log(Integer.toString(i));
					if (bHasKey)
					{
						String[][] wps = txtMining.posTagger.tag2Array(strs[i][1]);
						List<CoreLabel> rawWords = Sentence.toCoreLabelList(wps[0]);
						Tree parse = lp.apply(rawWords);
						StringWriter stringWriter = new StringWriter();
						PrintWriter writer = new PrintWriter(stringWriter);
						tp.printTree(parse, writer);
						StringBuffer sb = stringWriter.getBuffer();
						strs[i][2] = sb.toString();
						strs[i][3] = txtMining.GetDOTFromTree(parse,gsf);
						//DebugLog.Log(strs[i][2]); //不输出，提高处理速度
					}
				}
				// 输出结果到excel中
				ExcelExporter xls = new ExcelExporter(cfg.DependencyTreeFileName);
				xls.ExportFields(strs);
				xls.Close();
				DebugLog.Log(String.format("输出依赖树分析结果到 %s完成。", cfg.DependencyTreeFileName));
			}
			DebugLog.Log("数据分析完毕。");

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}



}
