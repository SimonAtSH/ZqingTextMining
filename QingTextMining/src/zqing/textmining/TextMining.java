package zqing.textmining;

public class TextMining
{

	public TextMining()
	{		
	}

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
			s.replaceAll("[\r\n]", ""); //去掉换行符
			if(!s.trim().isEmpty())	//去掉空行
				strAll += s + "\n";
		}
		
		strAll = strAll.replaceAll(",\n", ",");		// 替换英文逗号换行
		strAll = strAll.replaceAll("，\n", "，" );	// 替换中文逗号换行	
		String[] destLines = strAll.split("\n");		
		return destLines;
	}
	
	/*
	 * 把所有的文本行连接为一个String，用来进行断句
	 */
	
	public String GetConnectedString(String[] srcLines)
	{
		StringBuilder sb = new StringBuilder();
		for(String s:srcLines)
		{
			sb.append(s + "\n");
		}
		return sb.toString();
	}

}
