package com.hissp.distribution.module.promotion.convert.cms;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagSaveReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsTagRespVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsTagDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * CMS标签 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsTagConvert {

    CmsTagConvert INSTANCE = Mappers.getMapper(CmsTagConvert.class);

    CmsTagDO convert(CmsTagSaveReqVO bean);

    CmsTagRespVO convert(CmsTagDO bean);

    List<CmsTagRespVO> convertList(List<CmsTagDO> list);

    PageResult<CmsTagRespVO> convertPage(PageResult<CmsTagDO> page);

    AppCmsTagRespVO convertApp(CmsTagDO bean);

    List<AppCmsTagRespVO> convertAppList(List<CmsTagDO> list);

}
