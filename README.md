![Release](https://jitpack.io/v/fishedee/spring-boot-starter-batch-call.svg)
(https://jitpack.io/#fishedee/spring-boot-starter-batch-call)

# batch_call

Java的批量调用语法糖，功能：

* 注解优先，仅需用注解标注去哪里拉数据，对外抽象是逐次拉取，内部实现是批量拉取，简化批量拉取的代码量，优化可读性
* 性能好，自动收集多嵌套层里面的数据，仅需一次批量任务
* 多任务支持，通过指定name来启动具体的哪个任务。
* 单次批量最大值，可以控制每次拉取的批量最大值，以保护后端接口
* 支持在字段或者在方法上标注注解，这样能兼容Lomok生成的代码，也能支持动态生成key的场景
* 功能全，支持缓存，递归触发批量调用，允许是否为空数据，按顺序还是按key分发数据，支持泛型类的批量调用，单字段多任务支持

TODO功能：

* 多线程，可以支持同时触发多个批量，以最大化效率
* 单任务中多Class和多字段支持，目前仅能在单个任务中支持同一个Class，同一个字段上的批量调用。 

## 安装

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.fishedee</groupId>
        <artifactId>spring-boot-starter-batch-call</artifactId>
        <version>1.0</version>
    </dependency>
</dependencies>
```

在项目的pom.xml加入以上配置即可

## 使用

代码在[这里]()