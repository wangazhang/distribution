# 一、技术栈

## 1.1 后端技术栈

springboot2.7.2

mybatis-plus3.5.2 + mybatisX idea插件

redisson

## 1.2 小程序技术栈

https://gitee.com/binary/weixin-java-tools.git

## 1.3 代码反向生成

mybatis-plus3.5.2 + mybatisX idea插件

注意： DDD只生成DDD中的DO和相关Mapping文件

### 1.3.1 数据库设反向配置

#### 1.3.1.1 DO path

base path(基础路径):

`com.yehu.withus`

relative path(相对路径):

`infrastructure.fm.user.repository.database.dataobject`

#### 1.3.1.2 Xml path

`mybatis.fm.user`

#### 1.3.1.3 Mapper path

`com.yehu.withus.infrastructure.fm.user.repository.database`

#### 1.3.1.4 使用mybatisX

annotation: mybatis-plus3

option: comment

template: mybatis-plus3

# 插件

## maven  helper
