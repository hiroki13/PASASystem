# Predicate Argument Structure Analysis System

* "Joint Case Argument Identification for Japanese Predicate Argument Structure Analysis"
* In Proceedings of ACL 2015

## Preprocessing

* Input Data Format (NAIST Text Corpus)

```
\# S-ID:950112002-001 KNP:98/05/19 MOD:98/07/14
* 0 1D
地球 ちきゅう * 名詞 普通名詞 * * _
から から * 助詞 格助詞 * * _
* 1 2D
二千万 にせんまん * 名詞 数詞 * * _
光年 こうねん * 接尾辞 名詞性名詞助数辞 * * _
かなた かなた * 名詞 普通名詞 * * id="1"
に に * 助詞 格助詞 * * _
* 2 3D
ある ある * 動詞 * 子音動詞ラ行 基本形 alt="active"/ga="2"/ga_type="dep"/ni="1"/ni_type="dep"/type="pred"
```

* Command Line

```
python convert.py input_file_name output_file_name
```

## Training

* The input file is the preprocessed data format

```
java -jar PASASystem.jar -mode train -train train_data_name -test test_data_name -output output_file_name -iter 10 -restart 1 -weight 20000000
```

## Test

```
java -jar PASASystem.jar -mode test -test test_data_name -model model_file_name -output output_file_name -restart 1
```


