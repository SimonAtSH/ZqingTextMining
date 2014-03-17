package zqing.textmining.entity;

//����ʵ���Ϣ
public class WordEntity
{
	public String	Text;	//�ʵ�����
	public String	Type;	//�ʵ����ԣ��綯�ʣ����ʣ����ݴʵ�
	public long		Count;	//�ʵ�ͳ������
	public long 	Index;	//�ô��ڴʵ��е�����λ��
	public long		Possitive;	//�ʵ��������ֵ
	public long		Negative;  //�ʵĸ������ֵ
	
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
