title:Mybatis拦截器

date:2017年10月19日09:24:05

---

前言：最近项目有个需求，需要将所有产品的ID和Name组成Map传递到前台,以ID为key，以Name为Value。

即需要达到可以通过map.get(id)方便地获取name的值的效果。

然后就开始测试在Mybatis中哪种方法可行。首先看看直接使用resultType="map"

Mapper文件如下：

```java
<select id="testMap" resultType="map">
 select    id,name from coach 
</select>
```

CoachDao接口定义如下：

```
public Map<Object, Object> testMap();
```

测试方法如下：特意没有引入Spring，只有Mybatis方便DebugMybatis源码

```
	@Test
	public void testMap() {
		SqlSession session = null;
		System.out.println("Dd");
		try {

			session = MybatisUtil.getCurrentSession();
			CoachDao mapper = session.getMapper(CoachDao.class);
			Map<Object, Object> map = mapper.testMap();
			System.out.println(JSON.toJSONString(map));

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("testMap Error:{}", e);
		} finally {
			session.close();
		}
	}
```

我们debug观察下这次sql的执行过程。



我们从debug到MapperMethod这个类开始看

```
 public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    if (SqlCommandType.INSERT == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.insert(command.getName(), param));
    } else if (SqlCommandType.UPDATE == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.update(command.getName(), param));
    } else if (SqlCommandType.DELETE == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.delete(command.getName(), param));
    } else if (SqlCommandType.SELECT == command.getType()) {
      if (method.returnsVoid() && method.hasResultHandler()) {
        executeWithResultHandler(sqlSession, args);
        result = null;
      } else if (method.returnsMany()) {
        result = executeForMany(sqlSession, args);
      } else if (method.returnsMap()) {
        result = executeForMap(sqlSession, args);
      } else {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = sqlSession.selectOne(command.getName(), param);
      }
    } else {
      throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName() 
          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }
```

显然我们这里执行的是SqlCommandType.SELECT == command.getType()这一个分支的。

我们看看这个分支的流程，首先要判断是否参数中包含ResultHandler类型的参数，

然后判断是否是返回Collection容器类型或者数组类型，

接着判断是否Method方法参数中带有@MapKey注解且返回类型为Map,看到这里我们会想到这个是否能够实现我们想要的功能呢，这个我们接下来会测试。

然后终于到达我们我们要走的分支

执行

```java
 result = sqlSession.selectOne(command.getName(), param);
```



```java
  public <T> T selectOne(String statement, Object parameter) {
    // Popular vote was to return null on 0 results and throw exception on too many.
    List<T> list = this.<T>selectList(statement, parameter);
    if (list.size() == 1) {
      return list.get(0);
    } else if (list.size() > 1) {
      throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
    } else {
      return null;
    }
  }
```

我们发现实际上Mybatis给我们返回的是一个List，只不过当执行selectOne时，需要sql执行后返回的List集合中只有一个元素。而在这里我们知道上面的sql返回的是多行结果集，而多行结果集在没有经过插件处理的时候显然返回的List中有多个结果，这个时候就会报错。

```java
org.apache.ibatis.exceptions.TooManyResultsException: Expected one result (or null) to be returned by selectOne(), but found: 13

```

所以第一种方案不行。

那我们把方法的返回类型改一下：

```java
	public List<Map<Object, Object>> testMap();

```

可以保证程序正确运行，但是是List类型返回值而且其中的每一个Map是由两个元素构成的，分别以ID和NAME为键，即（"id":123）、("name":"Jack")的形式保存在Map中的。与我们想要的Map结构也不相同，虽然可以转化为我们想要的Map，但是这种转化不是很优雅，我们得找到一个更优雅更通用的方法。



- MapKey注解

  ​	那我们接着来试试之前用到的MapKey注解看能不能达到我想要的效果呢，查阅相关资料，我们开始尝试。

  我们更改方法,将方法带上注解

  ```java
  	@MapKey("ID")
  	public Map<Integer, Map<String, Object>> testMapKey();
  ```

  @MapKey("ID")这个注解表示最外层Map的key为查询结果中字段名为“id”的值。

  那此刻只要Mybatis有把Value设为查询结果中的name字段的值不就好了吗。

  但是很遗憾的是，Mybatis并没有直接实现这个功能。

  我们看selectMap源码，当我们在没有对查询结果使用插件的时候，我们存进Map的Value只能是我们查询结果集的指定返回类型。而我们这里resultType=“map”，也就是说返回的类型就是Map。那你说我们把resultType=“string”就可以了把，很显然，这里存在问题，因为我们的结果包含两列，不可能做到resulType=“string” 。所以使用MapKey注解也是无法达到我们想要的效果的。

  ```
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
      final List<?> list = selectList(statement, parameter, rowBounds);
      final DefaultMapResultHandler<K, V> mapResultHandler = new DefaultMapResultHandler<K, V>(mapKey,
          configuration.getObjectFactory(), configuration.getObjectWrapperFactory());
      final DefaultResultContext context = new DefaultResultContext();
      for (Object o : list) {
        context.nextResultObject(o);
        mapResultHandler.handleResult(context);
      }
      Map<K, V> selectedMap = mapResultHandler.getMappedResults();
      return selectedMap;
    }
  ```

  我们看看使用MapKey注解的返回情况。

  ```java
  ==>  Preparing: select id,name from coach 
  ==> Parameters: 
  <==    Columns: ID, NAME
  <==        Row: 12, phil jackson
  <==        Row: 17, phil jackson
  <==        Row: 1, jackson
  <==        Row: 14, phil jackson
  <==        Row: 18, phil jackson
  <==        Row: 2, jackson
  <==        Row: 10, jackson
  <==        Row: 13, phil jackson
  <==        Row: 15, phil jackson
  <==        Row: 16, phil jackson1
  <==        Row: 5, jackson
  <==        Row: 3, jackson
  <==        Row: 11, jackson
  <==      Total: 13
  {1:{"ID":1,"NAME":"jackson"},2:{"ID":2,"NAME":"jackson"},3:{"ID":3,"NAME":"jackson"},5:{"ID":5,"NAME":"jackson"},10:{"ID":10,"NAME":"jackson"},11:{"ID":11,"NAME":"jackson"},12:{"ID":12,"NAME":"phil jackson"},13:{"ID":13,"NAME":"phil jackson"},14:{"ID":14,"NAME":"phil jackson"},15:{"ID":15,"NAME":"phil jackson"},16:{"ID":16,"NAME":"phil jackson1"},17:{"ID":17,"NAME":"phil jackson"},18:{"ID":18,"NAME":"phil jackson"}}
  ```

  从结果集我们可以看出，以ID作为Key，但是Value是上面一种情况的Map集合，结构仍然为（（"id":123）、("name":"Jack")）显然是不符合要求的。



也就是说，很遗憾，我们的这种需求Mybatis并没有直接的支持，那我们该怎么办呢，这时候就需要用到Mybatis留给我们的重要接口Mybatis中的拦截器，通过使用提供给用户可以自定义实现特殊功能，其功能特别强大。我们可以在Mybatis最重要的四大组件中使用拦截器（通过动态代理和反射注解等技术实现,这三个技术就是框架的核心，其定义就不多说了），让我们能够自定义的对四大组件的功能进行丰富和更改等。

在说拦截器之前，我们需要说一下四大组件

这部分后续补充



### 二.Mybatis中的拦截器

现在我们来看Mybatis中的拦截器，Mybatis很贴心的为我们提供了Interceptor接口，作为一个优秀的开源框架，Mybatis和Spring一样都很好的遵守了开闭原则。基本都是面向接口的编程，特别是Spring，你会发现是Spring中大量的运用了模板方法模式来设计。在Mybatis中，我们要自定义我们的拦截器只需要实现这个接口。那我们首先就来瞅瞅这个接口都定义了哪些方法。

```java
public interface Interceptor {

  Object intercept(Invocation invocation) throws Throwable;

  Object plugin(Object target);

  void setProperties(Properties properties);

}
```

plugin方法是拦截器用于封装目标对象的，通过该方法我们可以返回目标对象本身，也可以返回一个它的代理。当返回的是代理的时候我们可以对其中的方法进行拦截来调用intercept方法(因为intercept方法的参数Invocation封装了原对象的相关信息)，当然也可以调用其他方法。setProperties方法是用于在Mybatis配置文件中指定一些属性的。       定义自己的Interceptor最重要的是要实现plugin方法和intercept方法，在plugin方法中我们可以决定是否要进行拦截进而决定要返回一个什么样的目标对象。而intercept方法就是要进行拦截的时候要执行的方法。而对于plugin方法而言，其实Mybatis已经为我们提供了一个实现。Mybatis中有一个叫做Plugin的类，里面有一个静态方法wrap(Object target,Interceptor interceptor)，通过该方法可以决定要返回的对象是目标对象还是对应的代理。Mybatis已经尽可能的帮我们做的更多了。

```
public class Plugin implements InvocationHandler {

  private Object target;
  private Interceptor interceptor;
  private Map<Class<?>, Set<Method>> signatureMap;

  private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }

  public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        return interceptor.intercept(new Invocation(target, method, args));
      }
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }

  private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
    if (interceptsAnnotation == null) { // issue #251
      throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());      
    }
    Signature[] sigs = interceptsAnnotation.value();
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<Class<?>, Set<Method>>();
    for (Signature sig : sigs) {
      Set<Method> methods = signatureMap.get(sig.type());
      if (methods == null) {
        methods = new HashSet<Method>();
        signatureMap.put(sig.type(), methods);
      }
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
      }
    }
    return signatureMap;
  }

  private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<Class<?>>();
    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[interfaces.size()]);
  }

}

```



我们先看一下Plugin的wrap方法，它根据当前的Interceptor上面的注解定义哪些接口需要拦截，然后判断当前目标对象是否有实现对应需要拦截的接口，如果没有则返回目标对象本身，如果有则返回一个代理对象。而这个代理对象的InvocationHandler正是一个Plugin。所以当目标对象在执行接口方法时，如果是通过代理对象执行的，则会调用对应InvocationHandler的invoke方法，也就是Plugin的invoke方法。(至于到底为什么会调用这个方法，我相信其实有不少人都不是真的明白，这个我在之前的博客中有说过，因为在调用代理对象的方法的时候实际上调用的就是InvocationHandler的invoke方法，InvocationHandler对象作为代理对象的构造器参数注入，要彻底明白

```java
 return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new Plugin(target, interceptor, signatureMap));
    }
```

这句话到底做了什么才能彻底理解JDK动态代理

)所以接着我们来看一下该invoke方法的内容。这里invoke方法的逻辑是：如果当前执行的方法是定义好的需要拦截的方法，则把目标对象、要执行的方法以及方法参数封装成一个Invocation对象，再把封装好的Invocation作为参数传递给当前拦截器的intercept方法。如果不需要拦截，则直接调用当前的方法。Invocation中定义了定义了一个proceed方法，其逻辑就是调用当前方法，所以如果在intercept中需要继续调用当前方法的话可以调用invocation的procced方法。

​       这就是Mybatis中实现Interceptor拦截的一个思想，如果用户觉得这个思想有问题或者不能完全满足你的要求的话可以通过实现自己的Plugin来决定什么时候需要代理什么时候需要拦截。以下讲解的内容都是基于Mybatis的默认实现即通过Plugin来管理Interceptor来讲解的。

​       对于实现自己的Interceptor而言有两个很重要的注解，一个是@Intercepts，其值是一个@Signature数组。@Intercepts用于表明当前的对象是一个Interceptor，而@Signature则表明要拦截的接口、方法以及对应的参数类型。

那在真正的去写自己的拦截器之前，我们必须得要知道Mybatis拦截器应该作用在什么地方。

而他又是怎么起作用的。之前我们说过Mybatis拦截器主要是作用在四大组件上的,那么是怎么起作用的呢。

我们还是看源码。

我们在执行一次SQL过程中进行debug，当debug到SimpleExecutor执行器时，执行了doQuery方法



```
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
      stmt = prepareStatement(handler, ms.getStatementLog());
      return handler.<E>query(stmt, resultHandler);
    } finally {
      closeStatement(stmt);
    }
  }
```

我们主要看这行代码：

这行代码实际上生生成了除了执行器的其他三大组件

```java
   StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
```



```
  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }
```

首先new RoutingStatementHandler();看这个类的源码，发现实际上生成了delegate属性的StatementHandler对象,而且实际上的StatementHandler这个组件的方法执行就是执行delegate的方法。

```
 public RoutingStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {

    switch (ms.getStatementType()) {
      case STATEMENT:
        delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      case PREPARED:
        delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      case CALLABLE:
        delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      default:
        throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
    }

  }
```

我们看构造方法总共有三个可选的StatementHandler。

我们发现这三个StatementHandler都继承了BaseStatementHandler,我们知道在创建一个子类实例对象是一定会先执行父类的构造方法，那我们来看看BaseStatementHandler的构造方法都做了什么工作。

```
  protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    this.configuration = mappedStatement.getConfiguration();
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;

    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();

    if (boundSql == null) { // issue #435, get the key before calculating the statement
      generateKeys(parameterObject);
      boundSql = mappedStatement.getBoundSql(parameterObject);
    }

    this.boundSql = boundSql;

    this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
  }
```

查看这个类的源码，我们会发现，她包含了其他两个组件，并且在构造方法里创建了这两个组件实现类的对象。

我们查看具体创建组件对象的源码，我们都会调用 interceptorChain.pluginAll方法

```
public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
  }

  public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,
      ResultHandler resultHandler, BoundSql boundSql) {
    ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
    return resultSetHandler;
  }

  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }

  public Executor newExecutor(Transaction transaction) {
    return newExecutor(transaction, defaultExecutorType);
  }

  public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
    } else {
      executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
  }

```

终于找到了使用拦截器的地方了。

我们看看InterceptorChain的源码。

```java
public class InterceptorChain {

  private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }
  
  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}

```

这个类是收集所有拦截器的类，那如何收集所有的拦截器呢，通过配置文件加载到Configuration对象中。

通过如下方式写入Mybatis配置文件。

```
      <plugins>  
        <plugin interceptor="com.wangcc.ssm.mybatis.interceptor.PageInterceptor">  
           <property name="databaseType" value="Oracle"/>  
       </plugin>  
   <plugin interceptor="com.wangcc.ssm.mybatis.interceptor.ParamMapInterceptor"/>  
</plugins>
```



### 三.实现自己的Mybatis拦截器

好了，回到我们要解决的需求，我们要通过Mybatis拦截器来解决问题了。

通过前面几种情况，我们知道了，Mybatis自带的返回类型处理不能满足我们这个需求的要求。所以我们理所当然的想到，我们使用拦截器的目标是ResultSetHandler。我们知道返回结果是由ResultSetHandler的handleResultSets方法对当前的Statement处理后的返回结果，所以我们如果要改变返回结果的话就需要使用Mybatis的拦截器对ResultSetHandler接口的handleResultSets方法进行拦截。

这个时候我们就确定了该拦截器的注解

```java
@Intercepts(@Signature(method = "handleResultSets", type = ResultSetHandler.class, args = { Statement.class }))

```

在之前分析四大组件对象生成时，说过每一个对象都要调用interceptorChain.pluginAll方法，这里的注解中的type = ResultSetHandler.class会使该拦截器只作用在ResultSetHandler上，但是目前来会对每一个ResultSetHandler都会起作用，所以我们还得在这个拦截器中想办法再做一层过滤，对于不需要拦截的调用Invocation的proceed()方法，而需要拦截的则实现我们自己的逻辑，返回对应的结果。现在我们要想的是怎么做这一层过滤以及怎么实现改变返回结果的效果。

- 首先，这个过滤该怎样来实现呢，一般来说，通过parameterObj来过滤， 通过ParameterHandler得到：parameterHandler.getParameterObject();（我们定义的Dao层方法的参数）

  ​	那这里我们需要给出一个怎样的参数呢：

  ​	首先他得满足这几个条件

  - （1）    可以指定哪个字段为返回Map的Key；
  - （2）    可以指定哪个字段为返回Map的Value；
  - （3）    可以附带其他参数；

那我们这里就给出我定义的参数ComplexParamMap

```java
@Data
public class ComplexParamMap<T> {
	private ParamMap paramMap;
	private T obj;// 使用实体类的属性作为查询参数
	private Map<String, Object> params = new HashMap<String, Object>();// 其他的参数我们把它分装成一个Map对象
}


public class ParamMap extends HashMap<String, String> {

	/**
	 * 作为Key的字段对应MapParam的Key
	 */
	public static final String KEY_FIELD = "mapKeyField";
	/**
	 * 作为Value的字段对应MapParam的Key
	 */
	public static final String VALUE_FIELD = "mapValueField";

	public ParamMap() {

	}

	/**
	 * 指定keyField和valueField
	 * 
	 * @param keyField
	 *            Map中key对应的字段
	 * @param valueField
	 *            Map中value对应的字段
	 */
	public ParamMap(String keyField, String valueField) {
		this.put(KEY_FIELD, keyField);
		this.put(VALUE_FIELD, valueField);
	}
}
```

使用泛型，方便 使用实体类的属性作为查询参数。

定义一个HashMap params 方便我们加入一些独立参数来作为查询参数。

ParamMap paramMap; 指定对应字段为返回Map的Key和Value；



- 确定了过滤条件之后,我们就可以开始处理我们的逻辑了。

  ​	

```java
@Intercepts(@Signature(method = "handleResultSets", type = ResultSetHandler.class, args = { Statement.class }))

public class ParamMapInterceptor implements Interceptor {
	private static Logger logger = LoggerFactory.getLogger(ParamMapInterceptor.class);

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		//
		Object target = invocation.getTarget();
			//目前Mybatis只有DefaultResultSetHandler这一种ResultSetHandler实现类
      if (target instanceof DefaultResultSetHandler) {
          
			DefaultResultSetHandler resultSetHandler = (DefaultResultSetHandler) target;

			ParameterHandler parameterHandler = ReflectUtil.getFieldValue(resultSetHandler, "parameterHandler");
			Object parameterObj = parameterHandler.getParameterObject();
			if (parameterObj instanceof ComplexParamMap) {
				ComplexParamMap paramMap = (ComplexParamMap) parameterObj;
				Statement stmt = (Statement) invocation.getArgs()[0];

				return handleResultSet(stmt.getResultSet(), paramMap);

			}

		}
		return invocation.proceed();
	}

	private Object handleResultSet(ResultSet resultSet, ComplexParamMap cmap) {
		if (resultSet != null) {
			ParamMap paramMap = cmap.getParamMap();
			String keyField = paramMap.get(ParamMap.KEY_FIELD);
			String valueField = paramMap.get(ParamMap.VALUE_FIELD);
			Map<Object, Object> map = new HashMap<Object, Object>();
			// 因为原方法返回的是List<Object>类型，所以
			List<Object> resultList = new ArrayList<Object>();
			try {
				while (resultSet.next()) {
					Object key = resultSet.getObject(keyField);
					Object value = resultSet.getObject(valueField);
					map.put(key, value);
				}
			} catch (Exception e) {
				logger.error("ParamMapInterceptor 的Method handleResultSet 使用ResultSet时出错");
			} finally {
				closeResultSet(resultSet);

			}
			resultList.add(map);
			return resultList;
		}
		return null;
	}

	private void closeResultSet(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (SQLException e) {
			logger.error("关闭ResultSet资源时出错");
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {

	}

}
```



到现在我们就算完成了我们的第一个Mybatis拦截器了。

我们将这个拦截器注册到我们的Mybatis配置文件，注意在配置文件中的顺序位置。

然后开始测试，看是否能达到我们的要求。

```
@Test
	public void test() {
		SqlSession session = null;
		System.out.println("Dd");
		try {

			session = MybatisUtil.getCurrentSession();
			CoachDao mapper = session.getMapper(CoachDao.class);
			// Coach coach = mapper.getCoachById(1);
			// System.out.println(coach);
			ComplexParamMap<Coach> cmap = new ComplexParamMap<Coach>();
			Coach coach = new Coach();
			coach.setName("jackson");
			ParamMap paramMap = new ParamMap("name", "age");
			cmap.setParamMap(paramMap);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", "jackson");
			cmap.setParams(params);
			cmap.setObj(coach);
			Map<Object, Object> map = mapper.queryMap(cmap);
			System.out.println(JSON.toJSONString(map));
			Page<Coach, ?> page = new Page<>();
			page.setSelf(coach);
			page.setPageSize(4);
			page.setPageNo(2);
			List<Coach> list = mapper.querybyPage(page);
			System.out.println(JSON.toJSONString(list));

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
```



Mapper配置文件

```
 <select id="queryMap" resultType="map" parameterType="ComplexParamMap">
 select id,name from coach where name=#{obj.name}
 </select>
```

查看输出结果

```
==> Parameters: jackson(String)
<==    Columns: ID, NAME
<==        Row: 1, jackson
<==        Row: 2, jackson
<==        Row: 10, jackson
<==        Row: 5, jackson
<==        Row: 3, jackson
<==        Row: 11, jackson
<==      Total: 6
{1:"jackson",2:"jackson",3:"jackson",5:"jackson",10:"jackson",11:"jackson"}
```



完美的结果。



接下来我们再看下在Mybatis中使用广泛的分页插件拦截器。