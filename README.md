## GeneralBean使用文档



### 注解介绍

1. `TargetColumnName`

标记do实体类对应的数据库表的名称



例如

 <img src="https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803111339497.png" alt="image-20210803111339497" style="zoom:50%;" />

对应的数据库表的结构为

<img src="https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803111408510.png" alt="image-20210803111408510" style="zoom: 50%;" />

> 几乎所有情况下都需要使用注解标注对应的do层数据库名称

- 如果因为疏忽或者其他什么原因忘记了标记对应的表名则会按照驼峰装下划线的方式将表名设置为对应的名称

  - 例如 do对象名称为 UserDo 则会默认 数据表名称为 user_do

  

2. `TargetDBOut`

   ​	一些情况下，可能我们的do对象中某些字段并不存在于数据库中仅仅只是为了数据的传递而存在，需要我们在对应的字段上面加上此注解，之后的字段解析则会跳过这个字段.

   <img src="https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803112008452.png" alt="image-20210803112008452" style="zoom:50%;" />

3. `TargetKeyColumn`

   - 通过这个注解设置我们的主键

   一般情况下，我们不希望你会使用到这个注解，因为主键名称应当于数据库中的一致。但是如果出现了不一致的情况下，你也许就需要使用到这个注解了。

    

   例如

    

   <img src="https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803112345566.png" alt="image-20210803112345566" style="zoom:50%;" />

> 注意这个注解是用来设置主键的,如果不加这个主键默认会选择 属性名称为“id” 的作为主键

这样在后面需要使用到主键的地方就会使用你定义的新主键名称来进行操作，例如批量更新，根据 主键的`update()`

4. `TargetColumnName`

这个注解你已经在上面的图片中见到了，他的作用很简单，就是给字段取别名。



### 在`service`中使用我们的gerneralBean

​	那么到了最重要的部分了，我们要使用我们的gerneralBean最核心的部分了。

**我们的gerneralBean现在只支持springboot项目的注入**

​	在我们的service层中注入`GeneralBeanService`即可

<img src="https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803113132121.png" alt="image-20210803113132121" style="zoom:50%;" />

你可以把这个当成一个service来看待。

### 开始使用

####  新增或者更新 save()

​		通过我们的save()方法 这个方法会根据 **主键id的值是否为空** 来决定是更新还是新增

```java
     User user = new User();
        user.setEmail("648669556@qq.com");
        user.setLoginName("测试别名");
        user.setUsername("陈俊宏");
        user.setPassword("password123");
        user.setStatus(1);
        user.setId(19);	  //这里设置了主键，则为更新
        generalBeanService.save(user);
```

#### 新增方法 add()

​	通过这个方法可以添加一个或者多个do对象到数据库中

```java
  User user = new User();
        user.setEmail("648669556@qq.com");
        user.setLoginName("测试别名");
        user.setUsername("陈俊宏");
        user.setPassword("password123");
        user.setStatus(1);
        generalBeanService.add(user);//新增单个do对象

  User user = new User();
        user.setEmail("648669556@qq.com");
        user.setLoginName("测试别名");
        user.setUsername("陈俊宏");
        user.setPassword("password123");
        user.setStatus(1);
User user2 = new User();
        user2.setEmail("648669556@qq.com");
        user2.setLoginName("测试别名");
        user2.setUsername("陈俊宏");
        user2.setPassword("password123");
        user2.setStatus(1);
        generalBeanService.add(Arrays.asList(user,user2)); //批量插入do对象 传入一个Collection<?> 对象
```

#### 修改do层对象 update

​	这里主要使用两种方式来更新

- 根据主键id来更新
- 根据条件构造器来更新

> 根据主键id来更新

```java
   User user = new User();
        user.setEmail("648669556@qq.com");
        user.setLoginName("测试别名");
        user.setUsername("陈俊宏");
        user.setPassword("password123");
        user.setStatus(1);
        user.setId(19);
        generalBeanService.update(user);
```

> 根据条件构造器来更新

条件构造器使用：

​	相信如果你使用过mybatis-plus 那么你一定会很快上手。

![image-20210803115122771](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803115122771.png)

这里是使用的lambda的方式来设置属性。

​	我们也推荐使用这种方式来设置条件。 传入 do对象的get方法引用，并设置对应的值。

当然你如果不喜欢这种方式，比较头铁，可以用下面的这种方式来使用.

![image-20210803115456122](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803115456122.png)

>  魔法值的出现，可不利于项目的维护！	

| 方法名称   | 方法作用                    |                                                              |
| ---------- | --------------------------- | ------------------------------------------------------------ |
| eq         | =                           |                                                              |
| ne         | <> 不等于                   |                                                              |
| gt         | >                           |                                                              |
| ge         | >=                          |                                                              |
| lt         | <                           |                                                              |
| le         | <=                          |                                                              |
| between    | a between b and c           |                                                              |
| notBetween | a not between b and c       |                                                              |
| like       | like ‘%a%’                  |                                                              |
| likeRight  | like 'a%'                   |                                                              |
| likeLeft   | like '%a'                   |                                                              |
| notLike    | not like '%a%'              |                                                              |
| isNull     | a is null                   |                                                              |
| isNotNull  | a is not null               |                                                              |
| in         | a in (1,2,3)                |                                                              |
| notIn      | a not in(1,2,3)             |                                                              |
| inSql      | a in ("自定义的sql语句")    |                                                              |
| notInSql   | a not in("自定义的sql语句") |                                                              |
| orderBy    | orderBy a                   |                                                              |
| or         | or                          | 默认在各个条件之间使用and连接，如果需要使用or请在条件之间连接or()方法 |



所有的方法都有重载，一般都是2个参数的，默认含义为 参数如果为空则不生成就好比xml里面的

```xml
<if test="a !=null ">
  
</if>
```

一样。当然如果有的属性你不想要这样的存在，可能需要你传入三个参数。

1. condition
   - 是否包含空值，如果为true ，即使你的值为null 依然会生效，如果为false 则值为null的话就不会生成这个条件。 默认为false 【不包含空值】
2. column
   - 传入列名，可以使用lamdba 的方法引用方式，或者字符串，上面介绍过了。
3. value
   - 你的值。

**还有一点就是目前所有的条件构造器，在条件中会默认存在一条 `deleted_at is null`** 

---------

#### 批量修改方法 batchUpdate()

​	在某些情况下我们会使用到批量的修改，来看一个例子吧。

```java
 User user = new User();
        user.setEmail("648669556@qq.com");
        user.setUsername("测试批量更新");
        user.setPassword("password123");
        user.setStatus(1);
        user.setId(18);
User user2 = new User();
        user2.setEmail("648669556@qq.com");
        user2.setUsername("测试批量更新");
        user2.setPassword("password123");
        user2.setStatus(1);
        user2.setId(19);
generalBeanService.batchUpdate(Arrays.asList(user,user2));
```

在批量更新方法中会根据主键id 去分别更新你对象里面不为空的值

同时这个方法也存在着重载。

```java
generalBeanService.batchUpdate(Arrays.asList(user,user2),false);//默认是使用这样的
```

我们可以在第二个参数传入true 这样你的参数即使为null也依然会被更新到。

-------

#### 使用do对象进行查询

在数据库中我们的查询操作还是非常多的。

我们可以使用do对象进行简单的查询。

![image-20210803144409482](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803144409482.png)

当然使用传入do对象的方式我们习惯明明成这样：

![image-20210803144456562](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803144456562.png)

留意一下，这样查询出来的结果，是存储在我们的分页工具pageSet里面的 我们可以使用`getResultList()`方法获取我们的list

![image-20210803144615774](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803144615774.png)

#### 查询一个单个对象的方法

![image-20210803144755567](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803144755567.png)

**异曲同工之妙！**

#### 根据条件构造器获取list

![image-20210803144949131](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803144949131.png)

**当然可以通过我们的条件构造器来获取数据啦。🌊**

#### 根据条件构造器获取分页list

![image-20210803145028964](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803145028964.png)

如果你想要分页的话，就在后面传入一个pageSet分页工具。就非常简单的实现了分页。

#### 根据条件构造器获取数据条数

![image-20210803145518509](https://myselfd.oss-cn-hangzhou.aliyuncs.com/uPic/image-20210803145518509.png)

