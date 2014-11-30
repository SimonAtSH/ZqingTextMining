package zqing.textmining.entity;

import java.util.ArrayList;
//import java.util.TreeMap;


public class LineEntity
{
	//public TreeMap<String, WordEntity> Words;
	public String 	Text;
	public String	DepTree;	//依赖树
	public ArrayList<LineEntity> SubLines;
	public long 	Index;	//该词在词典中的索引位置
	public long		Possitive;	//词的正面情感值
	public long		Negative;  //词的负面情感值
	public double	Motion;		//词的情感值
	
	public LineEntity()
	{
		//Words = new TreeMap<String, WordEntity>();
	}
	
	public LineEntity(String txt)
	{
		this.Text = txt;
	}
	
	public LineEntity(String txt, float motion)
	{
		this.Text = txt;
		this.Motion = motion;
	}
}
