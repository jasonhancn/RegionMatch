# 中国省市区（行政区划）匹配

这个项目来源于公司的内部需求，具体为在具备信息提取能力的系统中，使用省、市、区中一个或多个关键字来匹配具体民政部区域编码的功能。

开源版是在内部版的核心思想上，面向更广泛使用场景开发的版本，提供了更多（内部版并不需要的）用法。同时剔除了版权属于公司的代码和关键数据。此版本采用Apache v2协议开源，保证所有数据来源于公开网络。
- region.xlsx采集自民政部官方网站，按时间顺序排列，实际采集方式为行政区划代码信息页面全选->粘贴进入表格（匹配目标格式）
- region.csv可以使用test中的Builder类进行生成
- geo.csv中的编码/城市/GPS坐标信息采集自百度地图，不保证准确度

顺便甩个锅。。。
- 开源版的资源占用、性能都比内部版弱那么一点，优化过的代码拿了工资的，就不拿出来了（但是讲道理，其实速度没提升多少，这个逻辑也就这样了）
- Support Java7！但是观感上就比Java8的版本差了一些
- 基本随缘的格式检查（全靠提交的时候auto format），基本忽略静态检查的结果，有闲心了再去改

------

## 主要功能

- 可以通过民政部编码获取对应信息（支持1980年所有区级及以上地区的编码）
- 可以通过省、市、区各自的关键词匹配（支持模糊匹配）
- 可以通过任意输入匹配（支持模糊匹配）
- 支持调整匹配的策略

**不支持**
- 匹配部分输入文本（输入文本要完全包含在被匹配的数据中）
- 任何形式的通配符
- 停用词过滤（去除标点符号等，输入数据应只保留中文）

## 引用方法

maven central上传中...

## 接口

```java
RegionMatcher.byCode(int code)
```





