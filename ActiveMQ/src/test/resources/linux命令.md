title:linux命令

date：2017年10月27日13:28:30



---

使用cp  拷贝命令时 有时候会出现

```
cp: omitting directory `conf'

```



这个错误的意思是conf目录下还存在目录，所以不能直接拷贝。

这时我们需要使用递归拷贝，在cp命令后面加上-r参数，

```
cp -r conf conf2

```

