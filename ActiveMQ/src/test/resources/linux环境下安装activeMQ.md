title:linux环境下安装ActiveMQ

date：2017年10月26日19:07:16



---

因为真正的服务器环境都是linux下，所以，我们在做activemq集群服务的第一步就是在linux环境下安装ActiveMQ

首先，我们去官网下载linux环境下使用的activemq，我用的是apache-activemq-5.15.2-bin.tar.gz



1.将该文件放到linux服务器上的相应路径下

2.使用命令行打开该路径

```
cd  /ulic/ulimdev/activemq
```

3.解压该文件到当前目录

```
tar -zxvf apache-activemq-5.15.2-bin.tar.gz
```

这里说一下 zxvf的含义

x : 从 tar 包中把文件提取出来
z : 表示 tar 包是被 gzip 压缩过的，所以解压时需要用 gunzip 解压
v : 显示详细信息
f xxx.tar.gz :  指定被处理的文件是 xxx.tar.gz



linux tar 解压命令的使用

解压
语法：tar [主选项+辅选项] 文件或者目录

使用该命令时，主选项是必须要有的，它告诉tar要做什么事情，辅选项是辅助使用的，可以选用。
**主选项：**
c 创建新的档案文件。如果用户想备份一个目录或是一些文件，就要选择这个选项。相当于打包。

x 从档案文件中释放文件。相当于拆包。

t 列出档案文件的内容，查看已经备份了哪些文件。

特别注意，在参数的下达中， c/x/t 仅能存在一个！不可同时存在！因为不可能同时压缩与解压缩。
**辅助选项：**

-z ：是否同时具有 gzip 的属性？亦即是否需要用 gzip 压缩或解压？ 一般格式为xx.tar.gz或xx. tgz

-j ：是否同时具有 bzip2 的属性？亦即是否需要用 bzip2 压缩或解压？一般格式为xx.tar.bz2  

-v ：压缩的过程中显示文件！这个常用

-f ：使用档名，请留意，在 f 之后要立即接档名喔！不要再加其他参数！

-p ：使用原文件的原来属性（属性不会依据使用者而变）

--exclude FILE：在压缩的过程中，不要将 FILE 打包！



4.启动activemq

```
cd apache-activemq-5.15.2/bin
```

我们进入到Acitvemq的bin目录

然后ls一下

发现有以下一些目录

```
activemq       activemq.jar  linux-x86-32  macosx
activemq-diag  env           linux-x86-64  wrapper.jar

```

我们看到有linux32位和64位以及mac系统的选择

我们选择liunx-x86-64版本的进入

```
cd linux-x86-64
```

然后我们使用

```
./activemq start
```

```
./filename表示当前目录下的filename这个文件
```

启动activemq

验证下是否开启服务成功：

activemq服务默认端口61616，我们测试下61616端口是否被使用了。这个端口我们自己可以在配置文件里面更改。

```
 netstat -an|grep 61616

```





**2.格式**
grep [options]

**3.主要参数**
[options]主要参数：
－c：只输出匹配行的计数。
－I：不区分大 小写(只适用于单字符)。
－h：查询多文件时不显示文件名。
－l：查询多文件时只输出包含匹配字符的文件名。
－n：显示匹配行及 行号。
－s：不显示不存在或无匹配文本的错误信息。
－v：显示不包含匹配文本的所有行。
pattern正则表达式主要参数：
\： 忽略正则表达式中特殊字符的原有含义。
^：匹配正则表达式的开始行。
$: 匹配正则表达式的结束行。
\<：从匹配正则表达 式的行开始。
\>：到匹配正则表达式的行结束。
[ ]：单个字符，如[A]即A符合要求 。
[ - ]：范围，如[A-Z]，即A、B、C一直到Z都符合要求 。
。：所有的单个字符。
\* ：有字符，长度可以为0。

Linux系统中grep命令是一种强大的文本搜索工具，它能使用正则表达式搜索文本，并把匹 配的行打印出来。grep全称是Global Regular Expression Print，表示全局正则表达式版本，它的使用权限是所有用户。





linux一般使用netstat 来查看系统端口使用情况。这个命令以后会深入学习。

好了，现在我们就可以使用activemq了。

我们知道activemq有个管理界面，界面的服务器是用的jetty，登陆界面的用户名密码都是admin，端口默认是8161，像这些都是可以更改的。

更改用户名密码

```
[ulimdev@vm0003 bin]$ cd ..
[ulimdev@vm0003 apache-activemq-5.15.2]$ cd conf
[ulimdev@vm0003 conf]$ nano activemq.xml

```

打开activemq.xml后

在broker节点下添加这个节点就可以更改用户名和密码

```
<plugins>          
<simpleAuthenticationPlugin>
                                <users>
                                        <authenticationUser username="wangcc" p$
                                </users>
                        </simpleAuthenticationPlugin>
</puglins>


```



保存之后，我们来修改jetty.xml来更改管理界面的默认端口号 ，只需将以下节点更改即可

```
[ulimdev@vm0003 conf]$ nano jetty.xml
```



```
    <bean id="jettyPort" class="org.apache.activemq.web.WebConsolePort" init-me$
             <!-- the default port number for the web console -->
        <property name="host" value="0.0.0.0"/>
        <property name="port" value="8161"/>
    </bean>

```









