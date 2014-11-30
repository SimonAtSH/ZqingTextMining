package zqing.textmining;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import zqing.textmining.entity.WordEntity;
import zqing.textmining.input.CSVReader;
import zqing.textmining.output.CSVExporter;
import zqing.textmining.output.DebugLog;

public class TextMining
{

	public TextMining()
	{		
	}
	
	/**
	 * 附加词汇列表
	 */
	public String[] AddtionalWords = {
			"形容词",
			"名词", 
			"复词", 
			"动词", 
			"介词", 
			"时间短语", 
			"！", 
			"？", 
			"\\\\", 
			"#", 
			"字数"};

	/*
	 * 有些句子末尾以逗号结尾，下一句转到了新的一行，这表示句子并没有结束。所以要把这样的两行文本合并为一行。
	 * 如下例子：
	 *10770977135	2137901025	糖溶血红蛋白在7左右甚至到7.5都可以，千万别控制得太严格，而且要特别注意有没有低血糖，有没有心慌、出汗、头晕、晚上做恶梦，这些全都是低血糖的表现。   主持人：要特别注意这方面的内容。   李光伟：对。我老伴几年前查出有糖尿病，现在每一天服用二甲双胍，早中晚各一粒，血糖7以内，
	 *10770985953	2137901025	患糖尿病之前有高血压、心脏病，血压高压120，低压80，但是晚上常有心脏病发生。今年60岁，请问服药和饮食方面应该如何调理？   李光伟：看他胖不胖，如果比较胖，目前用这个药对他的好处非常多。因为二甲双胍是已经用了好几十年的药，医生对它比较了解，非常安全的一个药，而且不会产生低血糖，
	 *10770990895	2137901025	还会帮助减轻体重，对于相对胖的人来说是非常合适的一个药。而这个药也不会失效，单独用这个药，对于轻的糖尿病就可以把血糖控制很好，还不产生低血糖。所以，总体来说非常安全的一个药，而且价格也不是太贵。像这个病人如果吃二甲双胍，
	 *10770995535	2137901025	如果剂量合适，目前的血糖水平，餐后血糖应该也查一个，看看血糖怎么样。如果单独用二甲双胍就能把血糖控制好，这是非常好的一个方案。   但是随着病程的延长，单靠二甲双胍可能不行，还要加别的药，就得联合用药。联合用药的时候要特别注意二甲双胍的基础上再加上一个药，就有可能产生低血糖。
	 */
	public String[] ConnectBrokenLines(String[] srcLines)
	{
		String strAll = new String();
		for(String s:srcLines)
		{
			if(!s.trim().isEmpty())	//去掉空行
				strAll += s + "\n";
		}
		
		strAll = strAll.replaceAll(",\n", ",");		// 替换英文逗号换行
		strAll = strAll.replaceAll("，\n", "，" );	// 替换中文逗号换行	
		String[] destLines = strAll.split("\n");		
		return destLines;
	}
	
	/**
	 * 把所有的文本行连接为一个String，用来进行断句
	 * @param srcLines
	 * @return
	 */
	public String GetConnectedString(String[] srcLines)
	{
		StringBuilder sb = new StringBuilder();
		for(String s:srcLines)
		{
			sb.append(s);
		}
		return sb.toString();
	}
	
	/**
	 * 添加词汇到附加词汇列表AddtionalWords
	 * @param srcWords
	 * @return
	 */
	public String[] AddAddiontalWords(String[] srcWords)
	{
		int iCount = srcWords.length + AddtionalWords.length;
		String[] destWords = new String[iCount];
		int i = 0;
		for(String s:srcWords)
			destWords[i++] = s;
		for(String s: AddtionalWords)
			destWords[i++] = s;
		return destWords;
	}
	
	/**
	 * 从words[]数组中生成二元词词典，并统计词的出现次数,初始是出现一次
	 * @param words
	 * @param wordsDict
	 * @return
	 */
	public String[] GetBigramWordsArrayForOneLine(String[] words)
	{
		long wordsCount = words.length;
		// 一个句子分为 a b c d 四个词后，那么两两合并，会组合成 ab bc cd 三个二元词
		// 那么一个句子组合为二元词后数量应该为 wordsCount -1
		// 这里只是考虑一句话单行的问题
		String[] bigramWords = new String[(int) (wordsCount -1)];
		for(int i = 0; i < (wordsCount-1); i++)
		{
			bigramWords[i] = words[i] + words[i+1];
		}
		return bigramWords;
	}

	
	public TreeMap<String, WordEntity> GetWordsDict(String[] words, TreeMap<String, WordEntity> wordsDict)
	{
		return GetWordsDict(words, wordsDict, 1);
	}
	
	/**
	 * 从words[]数组中生成词典，并统计词的出现次数。
	 * @param words
	 * @param wordsDict
	 * @param defaultCount
	 * @return
	 */
	public TreeMap<String, WordEntity> GetWordsDict(String[] words, TreeMap<String, WordEntity> wordsDict, int defaultCount)
	{
		for (int i=0; i < words.length; i++)
		{
			if (!(words[i].trim().isEmpty()))
			{
				if (wordsDict.containsKey(words[i]))
				{
					WordEntity wEntity = wordsDict.get(words[i]);
					wEntity.Count++;
				} else
				{
					wordsDict.put(words[i], new WordEntity(words[i], defaultCount));
				}
			}
		}
		return wordsDict;
	}	
	
	/**
	 * 用来生成SVM格式的数据
	 * @param motion 情感极性值
	 * @param wordsOfLine	一句话分词并统计词频后得到的词典
	 * @param posOfLine		词性划分之后得到的对应的每个词的词性词典
	 * @param wordsDict		总的文章的分词后的词典
	 * @param posDict	总的文章的词性划分后的词性词典
	 * @return
	 */
	public String GenerateSVMLine(String motion, 
			TreeMap<String, WordEntity> wordsOfLine, TreeMap<String, WordEntity> posOfLine,
			TreeMap<String, WordEntity> wordsDict, TreeMap<String, WordEntity> posDict)
	{
		String svm = motion;
		//构造词汇部分
		for (Map.Entry<String, WordEntity> entry: wordsOfLine.entrySet()) 
		{
			String key = entry.getKey();
			WordEntity entity = entry.getValue();
			WordEntity dictEntity = wordsDict.get(key);
			if(entity != null && dictEntity != null)
			{
				svm += String.format(" %d:%d", dictEntity.Index, entity.Count);
			}
		}
		//构造词性部分
		for (Map.Entry<String, WordEntity> entry: posOfLine.entrySet()) 
		{
			String key = entry.getKey();
			WordEntity entity = entry.getValue();
			WordEntity dictEntity = posDict.get(key);
			if(entity != null && dictEntity != null)
			{
				svm += String.format(" %d:%d", dictEntity.Index, entity.Count);
			}
		}
		return svm;
	}
	
	public POSTagger posTagger = null;
	public CWSTagger cwsTag = null;
	/**
	 * 初始化分词工具CWSTagger和词性分词工具 POSTagger
	 * @return
	 */
	public boolean InitTagger()
	{
		boolean bResult = true;
		try
		{
			DebugLog.Log("Initiating CWSTagger");
			cwsTag = new CWSTagger("./models/seg.m", new edu.fudan.ml.types.Dictionary("./models/MotionDict.txt"));
			DebugLog.Log("Initiating POSTagger");
			// Bool值指定该词典是否用于cws分词（分词和词性可以使用不同的词典）// True就替换了之前的dict.txt
			posTagger = new POSTagger(cwsTag, "./models/pos.m", new edu.fudan.ml.types.Dictionary("./models/MotionDict.txt"), true);
			posTagger.removeDictionary(false);// 不移除分词的词典
			posTagger.setDictionary(new edu.fudan.ml.types.Dictionary("./models/MotionDict.txt"), false);// 设置POS词典，分词使用原来设置
		} catch (Exception e)
		{
			e.printStackTrace();
			bResult = false;
		}
		return bResult;	
	}
	
	public TreeMap<String, WordEntity> unigramWordsDict = null; // 一元词 词典
	public TreeMap<String, WordEntity> posDict = null;  // 词性词典
	public boolean GenerateUnigramWordsDict(String strConnectedTxt, String WordsTextFileName)
	{
		boolean bResult = false;
		try
		{
			if((posTagger == null) || (cwsTag == null))
				bResult = InitTagger();
			// 对全文分词并标注词性
			DebugLog.Log("POSTagger开始对全文进行分词和词性标注。");
			String[][] wordsAndPos = posTagger.tag2Array(strConnectedTxt); // 全文分词，词性标注

			// 生成词典
			DebugLog.Log("开始生成词典。");
			if(unigramWordsDict == null)
				unigramWordsDict = new TreeMap<String, WordEntity>();
			unigramWordsDict = this.GetWordsDict(wordsAndPos[0], unigramWordsDict);

			// 生成词性的词典
			if(posDict==null)
				posDict = new TreeMap<String, WordEntity>();
			posDict = this.GetWordsDict(this.AddtionalWords, posDict, 0);
			posDict = this.GetWordsDict(wordsAndPos[1], posDict, 1);
			DebugLog.Log("词典生成完毕。");

			// 将词典写入Words.txt文件中
			Set<String> wordsSet = unigramWordsDict.keySet();
			String[] wordsArray = wordsSet.toArray(new String[0]);
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(WordsTextFileName, wordsArray);

			// 将词性也写入Words.txt文件中。
			Set<String> posSet = posDict.keySet();
			String[] posArray = posSet.toArray(new String[0]);
			csvExport.ExportLines(WordsTextFileName, posArray, true); // Append的方式追加。
			DebugLog.Log(String.format("输出词典到%s完成。", WordsTextFileName));
			
			// 输出好词典文件后，设定wordsDict中每个key对应于词典的索引id。
			int index = 0;
			for (String s : wordsArray)
			{
				WordEntity w = unigramWordsDict.get(s);
				w.Index = index++;
			}
			// 设定词性对应于词典的索引ID
			for (String s : posArray)
			{
				WordEntity w = posDict.get(s);
				w.Index = index++;
			}			
			bResult = true;
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}		
		return bResult;
	}

	public TreeMap<String, WordEntity> bigramWordsDict = null; // 二元词 词典
	/**
	 * 生成二元词词典
	 * @param strConnectedTxt
	 * @param WordsTextFileName
	 * @return
	 */
	public boolean GenerateBigramWordsDict(String strConnectedTxt, String WordsTextFileName)
	{
		boolean bResult = false;
		try
		{
			if((posTagger == null) || (cwsTag == null))
				bResult = InitTagger();
			// 对全文分词并标注词性
			DebugLog.Log("CWSTagger开始对全文进行分词。");
			String[] unigramWordsArray = cwsTag.tag2Array(strConnectedTxt); // 全文一元词分词
			DebugLog.Log("CWSTagger分词完毕。");
			

			// 生成二元词 词组
			DebugLog.Log("两两合并生成二元词数组。");
			String[] bigramWordsArray = this.GetBigramWordsArrayForOneLine(unigramWordsArray);

			// 生成词典
			DebugLog.Log("开始生成词典。");
			if(bigramWordsDict == null)
				bigramWordsDict = new TreeMap<String, WordEntity>();			
			bigramWordsDict = this.GetWordsDict(bigramWordsArray, bigramWordsDict);

			DebugLog.Log("词典生成完毕。");

			// 将词典写入Words.txt文件中
			Set<String> wordsSet = bigramWordsDict.keySet();
			String[] wordsArray = wordsSet.toArray(new String[0]);
			CSVExporter csvExport = new CSVExporter();
			csvExport.ExportLines(WordsTextFileName, wordsArray);

			DebugLog.Log(String.format("输出词典到%s完成。", WordsTextFileName));
			
			// 输出好词典文件后，设定wordsDict中每个key对应于词典的索引id。
			int index = 0;
			for (String s : wordsArray)
			{
				WordEntity w = bigramWordsDict.get(s);
				w.Index = index++;
			}
			bResult = true;
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}		
		return bResult;
	}
	
	public boolean GenerateSVMData(String[] strTextLines, String[] strMotionValues, String AllSVMFileName, String TrainSVMFileName, String TestSVMFileName)
	{
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
		for (int i = 0; i < strTextLines.length; i++)
		{
			String[][] wps = this.posTagger.tag2Array(strTextLines[i]); // 分词并做词性标注
			if (wps == null)
				continue;

			for (int j = 0; j < wps.length; j++)
			{
				// edu.fudan.nlp.cn.tag.CWSTagger分词后似乎会在网址后加上一个\n，所以这里要去掉它
				wps[j][0] = wps[j][0].replaceAll("\n", "");
			}

			// 统计词频
			wordsMap.clear();
			wordsMap = this.GetWordsDict(wps[0], wordsMap);

			// 统计词性出现次数
			posMap.clear();
			posMap = this.GetWordsDict(wps[1], posMap);

			// 字数统计
			posMap.put("字数", new WordEntity("字数", strTextLines[i].length()));

			// 生成SVM数据
			motion = strMotionValues[i]; // 极性
			String svm = this.GenerateSVMLine(motion, wordsMap, posMap, this.unigramWordsDict, this.posDict);
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
		csvExport.ExportLines(AllSVMFileName, svmLines.toArray(new String[0]));
		DebugLog.Log(String.format("导出SVM数据%s完毕", AllSVMFileName));
		csvExport.ExportLines(TrainSVMFileName, trainList.toArray(new String[0]));
		DebugLog.Log(String.format("导出SVM数据%s完毕", TrainSVMFileName));
		csvExport.ExportLines(TestSVMFileName, testList.toArray(new String[0]));
		DebugLog.Log(String.format("导出SVM数据%s完毕", TestSVMFileName));
		return true;
	}
	
	private LexicalizedParser lp = null;
	private TreebankLanguagePack tlp = null;
	private GrammaticalStructureFactory gsf = null;
	private TreePrint tp = null;
	public void InitDepTreeParser(String TreePrintType)
	{
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/chinesePCFG.ser.gz");
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		tlp = lp.getOp().langpack();
		gsf = tlp.grammaticalStructureFactory();
		tp = new TreePrint(TreePrintType, tlp);
	}
	
	/**
	 * 生成依赖树，在生成依赖树之前要先调用InitDepTreeParser()进行初始化
	 * @param line
	 * @return
	 */
	public String GetDepTree(String line)
	{
		if((lp == null) || (lp ==null) || (gsf == null))
			return null;
		String depTree = "";
		String[][] wps = this.posTagger.tag2Array(line);
		List<CoreLabel> rawWords = Sentence.toCoreLabelList(wps[0]);
		Tree parse = lp.apply(rawWords);
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		tp.printTree(parse, writer);
		StringBuffer sb = stringWriter.getBuffer();
		depTree = sb.toString();
		return depTree;
		
	}
	
	public ArrayList<String[]> GenerateDepTreeAndDotLins(String[] strLines, String TreePrintType, String DependencyTreeFileName)
	{
		InitDepTreeParser(TreePrintType);
		// 输出极性，语句，和依赖树,和Graphviz的DOT图形描述到字符串数组中。
		int iLines = strLines.length;
		String[] strDepTrees = new String[iLines];
		String[] strDotLines = new String[iLines];
		for (int i = 0; i < iLines; i++)
		{
			DebugLog.Log(Integer.toString(i)); //
			String[][] wps = this.posTagger.tag2Array(strLines[i]);
			List<CoreLabel> rawWords = Sentence.toCoreLabelList(wps[0]);
			Tree parse = lp.apply(rawWords);
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(stringWriter);
			tp.printTree(parse, writer);
			StringBuffer sb = stringWriter.getBuffer();
			strDepTrees[i] = sb.toString();
			strDotLines[i] = this.GetDOTFromTree(parse,gsf);
			//DebugLog.Log(strDepTrees[i]); //不输出，提高处理速度
		}
		DebugLog.Log("输出依赖树分析结果完成。");
		ArrayList<String[]> resultList = new ArrayList<String[]>();
		resultList.add(strDepTrees);
		resultList.add(strDotLines);
		return resultList;
	}
	
	
	private HashSet<String> GetDictFromFile(String fileName, Integer initValue)
	{
		HashSet<String> dict = new HashSet<String>();
		try
		{
			CSVReader reader = new CSVReader();
			reader.LoadFromFile(fileName);
			String[] words = reader.GetFieldsByColumn(0);
			for(String s : words)
			{
				dict.add(s);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return dict;
	}
	
	/**
	 * 把传入的文本行进行分词，然后查找NTUSD_positive_simplified, 和NTUSD_negative_simplified，简单相加结果
	 * 得到的作为文本极性Baseline数据
	 * @param strExcelSrcData
	 * @return
	 */
	public int[] GenerateMotionBaseLine(String[] strLines)
	{
		//1. 加载Pos，Neg极性词典
		//2. 每句话分词
		//3. 每个词匹配Pos，Neg词典，匹配结果相加得到每句话的极性值
		//4. 输出极性值到excel表
		if((posTagger == null) || (cwsTag == null))
			InitTagger();
		HashSet<String> posDict = GetDictFromFile("./models/NTUSD_positive_simplified.txt", 1);
		HashSet<String> negDict = GetDictFromFile("./models/NTUSD_negative_simplified.txt", -1);
		int iLines = strLines.length;
		int[] resultArray = new int[iLines];
		for(int i = 0; i < iLines; i++ )
		{
			String srcLine = strLines[i];
			String[] words = this.cwsTag.tag2Array(srcLine);
			int iMotion = 0;
			for(int j = 0; j < words.length; j++)
			{
				if(posDict.contains(words[j]))
				{
					iMotion += 1;
				}
			}
			for(int j = 0; j < words.length; j++)
			{
				if(negDict.contains(words[j]))
				{
					iMotion += -1;
				}
			}
			resultArray[i] = iMotion;			
		}
		return resultArray;
	}
	
	
	public String GetDOTFromTree(Tree parse, GrammaticalStructureFactory gsf)
	{
		//生成Graphviz的DOT图形描述语言
		StringBuilder dotStr = new StringBuilder();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		Collection<TypedDependency> tdl = gs.typedDependencies();	
		dotStr.append("digraph g{\n edge [fontname=\"Simsun\"];\n node [fontname=\"SimSun\" size=\"16,16\"];\n{");
		for(TypedDependency depObj :tdl)
		{
			dotStr.append(depObj.gov().label().value() + "->" + 
						depObj.dep().label().value() + "[label=" + 
						depObj.reln().toString() + "];\n");
		}
		dotStr.append("}\n}");
		return dotStr.toString();
	}
	
	/**
	 * 查找key在str中出现的次数
	 * @param str
	 * @param key
	 * @return
	 */
	public int CountStringKeyNumber(String str, String key)
	{
		int count = 0;
		for (String tmp = str; tmp != null && tmp.length() >= key.length();)
		{
			if (tmp.indexOf(key) == 0)
			{
				count++;
				tmp = tmp.substring(key.length());
			} else
			{
				tmp = tmp.substring(1);
			}
		}
		return count;
	}

	/**
	 * ?
	 * @param strDepLines
	 * @param relationsToFind
	 * @return
	 */
	public int[] GetDepRelationCount(String[] strDepLines, String[] relationsToFind)
	{
		int iCount = strDepLines.length;
		int[] depCounts = new int[iCount];
		for (int i = 0; i < iCount; i++)
		{
			for (int j = 0; j < relationsToFind.length; j++)
			{
				if (j > 0)
					depCounts[i] = depCounts[i] * CountStringKeyNumber(strDepLines[i], relationsToFind[j]);
				else
					depCounts[i] = CountStringKeyNumber(strDepLines[i], relationsToFind[j]);
			}
		}
		return depCounts;
	}
}
