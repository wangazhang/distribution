package com.hissp.distribution.module.infra.dal.dataobject.image;

import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 图片文件夹表
 *
 * @author hissp
 */
@TableName("infra_image_folder")
@KeySequence("infra_image_folder_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageFolderDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    private Long id;

    /**
     * 文件夹名称
     */
    private String name;

    /**
     * 父文件夹ID，0表示根目录
     */
    private Long parentId;

    /**
     * 文件夹层级
     */
    private Integer level;

    /**
     * 完整路径
     */
    private String path;

    /**
     * 权限类型：1-共享，2-隐私
     */
    private Integer permissionType;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;

}