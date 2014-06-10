package zqing.textmining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import zqing.textmining.entity.WordEntity;
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
	 * 从words[]数组中生成词典，并统计词的出现次数,初始是出现一次
	 * @param words
	 * @param wordsDict
	 * @return
	 */
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
	public TreeMap<String, WordEntity> wordsDict = null;
	public TreeMap<String, WordEntity> posDict = null;
	public boolean GenerateWordsDict(String strConnectedTxt, String WordsTextFileName)
	{
		boolean bResult = false;
		try
		{
			DebugLog.Log("Initiating CWSTagger");
			CWSTagger cwsTag;
			cwsTag = new CWSTagger("./models/seg.m", new edu.fudan.ml.types.Dictionary("./models/dict.txt"));
			DebugLog.Log("Initiating POSTagger");
			// Bool值指定该词典是否用于cws分词（分词和词性可以使用不同的词典）// True就替换了之前的dict.txt
			posTagger = new POSTagger(cwsTag, "./models/pos.m", new edu.fudan.ml.types.Dictionary("./models/dict.txt"),
					true);
			posTagger.removeDictionary(false);// 不移除分词的词典
			posTagger.setDictionary(new edu.fudan.ml.types.Dictionary("./models/dict.txt"), false);// 设置POS词典，分词使用原来设置
			// 对全文分词并标注词性
			DebugLog.Log("POSTagger开始对全文进行分词和词性标注。");
			String[][] wordsAndPos = posTagger.tag2Array(strConnectedTxt); // 全文分词，词性标注

			// 生成词典
			DebugLog.Log("开始生成词典。");
			if(wordsDict == null)
				wordsDict = new TreeMap<String, WordEntity>();
			wordsDict = this.GetWordsDict(wordsAndPos[0], wordsDict);

			// 生成词性的词典
			if(posDict==null)
				posDict = new TreeMap<String, WordEntity>();
			posDict = this.GetWordsDict(this.AddtionalWords, posDict, 0);
			posDict = this.GetWordsDict(wordsAndPos[1], posDict, 1);
			DebugLog.Log("词典生成完毕。");

			// 将词典写入Words.txt文件中
			Set<String> wordsSet = wordsDict.keySet();
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
				WordEntity w = wordsDict.get(s);
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
	
	public ArrayList<String> svmLines = new ArrayList<String>();
	public ArrayList<String> trainList = new ArrayList<String>(); // Train 数据包含70%的SVM数据
	public ArrayList<String> testList = new ArrayList<String>(); // Test 数据包含30%的SVM数据
	public boolean GenerateSVMData(String[][] strExcelSrcData)
	{
		return true;
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
}
