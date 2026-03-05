-- 初始化
-- 删除阿章
select id INTO @userId  from system_social_user where openid='omikT5b0RcGA3ShCaMy1jRp1YmYg';
delete from member_user ms  WHERE ms.id=@userId;
delete from system_social_user_bind sub WHERE sub.user_id =@userId;
delete from system_social_user su WHERE su.id =@userId;
-- 保留用户创建及分佣用户关系
-- TRUNCATE TABLE member_user;
-- TRUNCATE TABLE trade_brokerage_user;
-- TRUNCATE TABLE system_social_user;
-- TRUNCATE TABLE system_social_user_bind;
-- 保留用户余额
-- TRUNCATE TABLE pay_wallet;
-- TRUNCATE TABLE pay_wallet_transaction;
-- 清空用户分佣余额  及分销资格
update trade_brokerage_user tbu SET tbu.brokerage_price =0 , tbu.frozen_price=0,tbu.brokerage_enabled=1;
update member_user ms SET ms.level_id=4 WHERE ms.id=1;
update member_user ms SET ms.level_id=3 WHERE ms.id=2;
update member_user ms SET ms.level_id=3 WHERE ms.id=3;
-- update member_user ms SET ms.level_id=1 WHERE ms.id=4;
update pay_wallet pw SET pw.balance=10000000;
update trade_brokerage_user tbu SET tbu.brokerage_price =0 , tbu.frozen_price=0,tbu.brokerage_enabled=0 where tbu.id=4;

TRUNCATE TABLE mb_material_record;
TRUNCATE TABLE mb_user_material_order;
TRUNCATE TABLE mb_material_outbound;
TRUNCATE TABLE mb_material_outbound_item;

TRUNCATE TABLE member_experience_record;
TRUNCATE TABLE member_level_record;
TRUNCATE TABLE member_point_record;

TRUNCATE TABLE trade_order;
TRUNCATE TABLE trade_order_item;

TRUNCATE TABLE trade_after_sale;
TRUNCATE TABLE trade_after_sale_log;

TRUNCATE TABLE product_comment;

TRUNCATE TABLE trade_brokerage_withdraw;
TRUNCATE TABLE trade_brokerage_record;

TRUNCATE TABLE trade_statistics;

TRUNCATE TABLE pay_order;
TRUNCATE TABLE pay_order_extension;
TRUNCATE TABLE pay_refund;
TRUNCATE TABLE pay_notify_task;




-- 日志表清理

TRUNCATE TABLE infra_api_access_log;
TRUNCATE TABLE infra_api_error_log;
TRUNCATE TABLE infra_job_log;
TRUNCATE TABLE pay_notify_log;
TRUNCATE TABLE system_login_log;
TRUNCATE TABLE system_mail_log;
TRUNCATE TABLE system_operate_log;
TRUNCATE TABLE system_sms_log;
TRUNCATE TABLE trade_order_log;
TRUNCATE TABLE trade_after_sale_log;

SELECT * from member_user;
SELECT* from system_social_user;
SELECT * from system_social_user_bind;
SELECT * from trade_brokerage_user;
SELECT * from mb_user_material_order;
SELECT * from mb_material_record;
SELECT * from mb_material_refill_map;
SELECT * from pay_order;
SELECT * from pay_notify_task;
