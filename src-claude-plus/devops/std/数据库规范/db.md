1、使用'id' 命名主键，被其他表引用的主键，使用表名+'_id',来命名主键 <br>
2、事实表，record； order_record<br>
3、维度表，entity； user<br>
4、关系表，关系表以'_'分割，如：user_role_relation<br>
5、所有表的主键不允许使用数据库自增，而需要使用分布式id生成器<br>
6、索引名称以idx打头,中杠线区分字段，下划线作为完整表名称、字段名称链接符号。如idx-user_id<br>
7、