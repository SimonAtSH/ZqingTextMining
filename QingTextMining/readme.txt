该程序用Stanford parser对文本进行分词，词性标注，然后生成依赖树关系，然后生成DOT图形描述，可用Graphviz生成对应的依赖树的图形。
ExcelDataToSVMLight 运行参数：
-InExcel C:\works\simon\workspace\QingTextMining\exampleData\NegResult\Neg.xls -OutFolder C:\works\simon\workspace\QingTextMining\exampleData\NegResult\ -DepTree

文本断句，输出到excel文件中。
TextSentenizer 运行参数：
-InText C:\works\simon\workspace\QingTextMining\exampleData\Neg.txt  -OutFolder C:\works\simon\workspace\QingTextMining\exampleData\