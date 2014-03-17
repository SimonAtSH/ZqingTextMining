package zqing.textmining.entity;

//保存词的信息
public class WordEntity
{
	public String	Text;	//词的内容
	public String	Type;	//词的属性，如动词，名词，形容词等
	public long		Count;	//词的统计数量
	public long 	Index;	//该词在词典中的索引位置
	public long		Possitive;	//词的正面情感值
	public long		Negative;  //词的负面情感值
	
	public WordEntity()
	{
		Count = 0;
		Possitive = 0;
		Negative = 0;
	}
	
	public WordEntity(String txt, long c)
	{
		Text = txt;
		Count = c;
	}
}
