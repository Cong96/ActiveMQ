title：Java自动装箱和拆箱

date：2017年10月27日15:04:35



---







```java
public static Integer valueOf(inti) {    
    if(i >= -128 &&i <=IntegerCache.high)    
       //如果i在-128~high之间,就直接在缓存中取出i的Integer类型对象  
       return IntegerCache.cache[i + 128];    
    else  
       return new Integer(i); //否则就在堆内存中创建   
}    
```



```java
  private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // high value may be configured by property
            int h = 127;//h值，可以通过设置jdk的AutoBoxCacheMax参数调整
            String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                 // 取较大的作为上界，但又不能大于Integer的边界MAX_VALUE  

                    i = Math.max(i, 127);//上界最小为127
                    // Maximum array size is Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // If the property cannot be parsed into an int, ignore it.
                }
            }
            high = h;

            cache = new Integer[(high - low) + 1]; //上界确定，此时high默认一般是127  
        // 创建缓存块，注意缓存数组大小  
            int j = low;
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);// -128到high值逐一分配到缓存数组  

            // range [-128, 127] must be interned (JLS7 5.1.7)
            assert IntegerCache.high >= 127;
        }

        private IntegerCache() {}
    }

```

我们将上述代码重要部分都加上了注释，我们特别注意在确定i的取值时，我们标注了i的最小值是127，也就是说

Integer的缓存至少要覆盖[-128, 127]的范围,那么为什么符合规范的Java实现必须保证Integer的缓存至少要覆盖[-128, 127]的范围

这个有官方说明：(The Java Language Specification, 3rd Edition)

```
If the value p being boxed is true, false, a byte, a char in the range \u0000 to \u007f, or an int or short number between -128 and 127, then let r1 and r2 be the results of any two boxing conversions of p. It is always the case that r1 == r2.
```

为了便于理解，我们给出大概的中文意思

```
为了节省内存，对于下列包装对象的两个实例，当它们的基本值相同时，他们总是==：  
 Boolean  
 Byte  
 Character, \u0000 - \u007f(7f是十六进制的127)  
 Integer, -128 — 127  
```

这里给出除了Integer之外的其他类型的自动装箱池

```
Boolean：(全部缓存)
Byte：(全部缓存 -128 — 127)

Character(0-127缓存)
Short(-128 — 127缓存)
Long(-128 — 127缓存)

Float(没有缓存)
Doulbe(没有缓存)
```

上面一开始有说过，我们可以通过设置VM虚拟机参数来动态的改变Integer缓存区间大小。通过设置

-XX:AutoBoxCacheMax=NNN参数即可将Integer的自动缓存区间设置为[-128,NNN]。这个参数是server模式专有的。

首先你要确认你的Java服务运行所用到的jdk的默认运行模式是多少，使用javac -version参看，我本机安装的JDK8默认就是Server 模式。

如果你默认的是Client模式的话，你就需要作出一些调整了，找到你所安装jdk的地方，进入到jdk/jre/bin目录，然后搜索jvm.cfg文件，修改文件，将Server Client两行顺序做一个前后调整就好。



对于垃圾回收器来说：

```java
Integer i = 100;     
i = null;//will not make any object available for GC at all.  
```

这里的代码不会有对象符合垃圾回收器的条件，这儿的i虽然被赋予null，但它之前指向的是cache中的Integer对象，而cache没有被赋null，所以Integer(100)这个对象还是存在。



总结

   Java使用自动装箱和拆箱机制，节省了常用数值的内存开销和创建对象的开销，提高了效率。通过上面的研究和测试，结论如下：

​        （1）Integer和 int之间可以进行各种比较；Integer对象将自动拆箱后与int值比较

​        （2）两个Integer对象之间也可以用>、<等符号比较大小；两个Integer对象都拆箱后，再比较大小

​        （3） 两个Integer对象最好不要用==比较。因为：-128~127范围(一般是这个范围)内是取缓存内对象用，所以相等，该范围外是两个不同对象引用比较，所以不等。