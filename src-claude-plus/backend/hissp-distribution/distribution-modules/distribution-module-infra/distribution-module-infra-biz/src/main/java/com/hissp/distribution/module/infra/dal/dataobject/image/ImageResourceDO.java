package com.hissp.distribution.module.infra.dal.dataobject.image;

import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 图片资源表
 *
 * @author hissp
 */
@TableName("infra_image_resource")
@KeySequence("infra_image_resource_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResourceDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    private Long id;

    /**
     * 关联文件表ID
     */
    private Long fileId;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 所属文件夹ID，0表示根目录
     */
    private Long folderId;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 图片格式
     */
    private String format;

    /**
     * 图片访问URL
     */
    private String url;

    /**
     * 图片标签，逗号分隔
     */
    private String tags;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 查看次数
     */
    private Integer viewCount;

}