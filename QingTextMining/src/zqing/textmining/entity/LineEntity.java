package zqing.textmining.entity;

import java.util.TreeMap;


public class LineEntity
{
	public TreeMap<String, WordEntity> Words;
	
	public LineEntity()
	{
		Words = new TreeMap<String, WordEntity>();
	}
}
