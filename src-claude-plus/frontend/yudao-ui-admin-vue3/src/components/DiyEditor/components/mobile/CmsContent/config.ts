import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'
import { cloneDeep } from 'lodash-es'

/** CMS内容组件属性 */
export interface CmsContentProperty {
  // 基础配置 - 只保留板块选择
  sectionId: number | null  // 板块ID（必填）

  // 组件样式
  style: ComponentStyle
}

/** CMS内容组件 */
export const component = {
  id: 'CmsContent',
  name: 'CMS内容',
  icon: 'ep:reading',
  property: {
    // 基础配置 - 只保留板块选择
    sectionId: null,

    // 样式配置
    style: {
      bgType: 'color',
      bgColor: '#f5f5f5',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8,
      padding: 0,
      paddingTop: 0,
      paddingRight: 0,
      paddingBottom: 0,
      paddingLeft: 0,
      borderRadius: 0,
      borderTopLeftRadius: 0,
      borderTopRightRadius: 0,
      borderBottomRightRadius: 0,
      borderBottomLeftRadius: 0
    } as ComponentStyle
  }
} as DiyComponent<CmsContentProperty>
