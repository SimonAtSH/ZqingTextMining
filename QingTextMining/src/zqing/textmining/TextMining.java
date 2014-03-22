package zqing.textmining;

import java.util.Map;
import java.util.TreeMap;

import zqing.textmining.entity.WordEntity;

public class TextMining
{

	public TextMining()
	{		
	}
	
	public String[] AddtionalWords = {
			"���ݴ�",
			"����", 
			"����", 
			"����", 
			"���", 
			"ʱ�����", 
			"��", 
			"��", 
			"\\", 
			"#", 
			"����"};

	/*
	 * ��Щ����ĩβ�Զ��Ž�β����һ��ת�����µ�һ�У����ʾ���Ӳ�û�н���������Ҫ�������������ı��ϲ�Ϊһ�С�
	 * �������ӣ�
	 *10770977135	2137901025	����Ѫ�쵰����7����������7.5�����ԣ�ǧ�����Ƶ�̫�ϸ񣬶���Ҫ�ر�ע����û�е�Ѫ�ǣ���û���Ļš�������ͷ�Ρ����������Σ���Щȫ���ǵ�Ѫ�ǵı��֡�   �����ˣ�Ҫ�ر�ע���ⷽ������ݡ�   ���ΰ���ԡ����ϰ鼸��ǰ��������򲡣�����ÿһ����ö���˫�ң��������һ����Ѫ��7���ڣ�
	 *10770985953	2137901025	������֮ǰ�и�Ѫѹ�����ಡ��Ѫѹ��ѹ120����ѹ80���������ϳ������ಡ����������60�꣬���ʷ�ҩ����ʳ����Ӧ����ε���   ���ΰ�������ֲ��֣�����Ƚ��֣�Ŀǰ�����ҩ�����ĺô��ǳ��ࡣ��Ϊ����˫�����Ѿ����˺ü�ʮ���ҩ��ҽ�������Ƚ��˽⣬�ǳ���ȫ��һ��ҩ�����Ҳ��������Ѫ�ǣ�
	 *10770990895	2137901025	��������������أ���������ֵ�����˵�Ƿǳ����ʵ�һ��ҩ�������ҩҲ����ʧЧ�����������ҩ������������򲡾Ϳ��԰�Ѫ�ǿ��ƺܺã�����������Ѫ�ǡ����ԣ�������˵�ǳ���ȫ��һ��ҩ�����Ҽ۸�Ҳ����̫���������������Զ���˫�ң�
	 *10770995535	2137901025	����������ʣ�Ŀǰ��Ѫ��ˮƽ���ͺ�Ѫ��Ӧ��Ҳ��һ��������Ѫ����ô������������ö���˫�Ҿ��ܰ�Ѫ�ǿ��ƺã����Ƿǳ��õ�һ��������   �������Ų��̵��ӳ�����������˫�ҿ��ܲ��У���Ҫ�ӱ��ҩ���͵�������ҩ��������ҩ��ʱ��Ҫ�ر�ע�����˫�ҵĻ������ټ���һ��ҩ�����п��ܲ�����Ѫ�ǡ�
	 */
	public String[] ConnectBrokenLines(String[] srcLines)
	{
		String strAll = new String();
		for(String s:srcLines)
		{
			if(!s.trim().isEmpty())	//ȥ������
				strAll += s + "\n";
		}
		
		strAll = strAll.replaceAll(",\n", ",");		// �滻Ӣ�Ķ��Ż���
		strAll = strAll.replaceAll("��\n", "��" );	// �滻���Ķ��Ż���	
		String[] destLines = strAll.split("\n");		
		return destLines;
	}
	
	/*
	 * �����е��ı�������Ϊһ��String���������жϾ�
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
	
	public TreeMap<String, WordEntity> GetWordsDict(String[] words, TreeMap<String, WordEntity> wordsDict)
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
					wordsDict.put(words[i], new WordEntity(words[i], 1));
				}
			}
		}
		return wordsDict;
	}
	
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
	
	public String GenerateSVMLine(String motion, 
			TreeMap<String, WordEntity> wordsOfLine, TreeMap<String, WordEntity> posOfLine,
			TreeMap<String, WordEntity> wordsDict, TreeMap<String, WordEntity> posDict)
	{
		String svm = motion;
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
		
		svm += " # ";
		for (Map.Entry<String, WordEntity> entry: wordsOfLine.entrySet()) 
		{
			svm += entry.getKey() + " ";
		}
		for (Map.Entry<String, WordEntity> entry: posOfLine.entrySet()) 
		{
			svm += entry.getKey() + " ";
		}		
		svm = svm.replace('\n', ' ');
		svm = svm.replaceAll("\n", "");
		return svm;
	}

}
