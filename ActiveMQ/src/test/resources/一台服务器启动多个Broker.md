title:ActiveMQ集群搭建

date:2017年10月27日13:18:44



---

在真正的生产环境中，我们一般使用的都是集群环境，不过我们这里只有一台linux服务器，我们只能在一台服务器上实现一下集群了。首先我们来完成一台服务器上启动多个broker的功能

1.进入到activemq目录，拷贝conf文件到conf2,然后打开conf2，修改目录下的activemq.xml文件



```
[ulimdev@vm0003 apache-activemq-5.15.2]$ cp -r conf conf2
[ulimdev@vm0003 apache-activemq-5.15.2]$ cd conf2
[ulimdev@vm0003 conf2]$ nano activemq.xml

```

我们要对activemq的哪些内容进行修改呢：

- brokerName，位于broker节点，这个需要更改，不能和原名称相同。
- 默认缓存地址不能一样，<kahaDB directory="${activemq.data}/kahadb_2"/>
- 更改transportConnectors节点中出现的所有端口。我们将常用的tcp协议对应端口从61616改为61617

2.修改jetty.xml

```java
 nano jetty.xml
```



修改jetty.xml配置文件中的端口号，这个是为了登陆activemq管理页面的地址不冲突，这里我们改为8162端口。

3.返回到bin目录，复制actiemq文件 命名为activemq2

```
[ulimdev@vm0003 conf2]$ cd ../bin
[ulimdev@vm0003 bin]$ cp activemq activemq2
[ulimdev@vm0003 bin]$ nano activemq2

```

打开activemq2之后，我们search(Crtl+W):ACTIVEMQ_PIDFILE(不区分大小写) ，然后修改对应的值

然后search：ACTIVEMQ_CONF，修改对应的值。

4.启动两个broker

```
[ulimdev@vm0003 bin]$ ls
activemq  activemq2  activemq-diag  activemq.jar  env  linux-x86-32  linux-x86-64  macosx  wrapper.jar
[ulimdev@vm0003 bin]$ cd linux-x86-64
[ulimdev@vm0003 linux-x86-64]$ ./activemq stop
Stopping ActiveMQ Broker...
Stopped ActiveMQ Broker.
[ulimdev@vm0003 linux-x86-64]$ cd ../activemq start
-bash: cd: ../activemq: Not a directory
[ulimdev@vm0003 linux-x86-64]$ cd ..
[ulimdev@vm0003 bin]$ ./activemq start
INFO: Loading '/ulic/ulimdev/activemq/apache-activemq-5.15.2//bin/env'
INFO: Using java '/usr/local/java/jdk1.8.0_144/bin/java'
INFO: Starting - inspect logfiles specified in logging.properties and log4j.properties to get details
INFO: pidfile created : '/ulic/ulimdev/activemq/apache-activemq-5.15.2//data/activemq.pid' (pid '3794')
[ulimdev@vm0003 bin]$ ./activemq2 start
INFO: Loading '/ulic/ulimdev/activemq/apache-activemq-5.15.2//bin/env'
INFO: Using java '/usr/local/java/jdk1.8.0_144/bin/java'
INFO: Starting - inspect logfiles specified in logging.properties and log4j.properties to get details
INFO: pidfile created : '/ulic/ulimdev/activemq/apache-activemq-5.15.2//data/activemq2.pid' (pid '3883')

```

然后我们登陆管理界面查看，返现两个管理界面都已经成功登陆，表明启动多个broker成功。