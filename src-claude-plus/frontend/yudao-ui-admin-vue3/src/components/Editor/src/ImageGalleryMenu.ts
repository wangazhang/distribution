import { IButtonMenu, IDomEditor, Boot } from '@wangeditor/editor'

/**
 * 图库选择菜单
 */
class ImageGalleryMenu implements IButtonMenu {
  readonly title = '图库选择'
  readonly iconSvg =
    '<svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M928 160H96c-17.7 0-32 14.3-32 32v160h896V192c0-17.7-14.3-32-32-32zM64 896c0 17.7 14.3 32 32 32h832c17.7 0 32-14.3 32-32V440H64v456zm128-212h64v64H192v-64zm128 0h256v64H320v-64z" fill="currentColor"></path></svg>'
  readonly tag = 'button'

  /**
   * 获取菜单值
   */
  getValue(editor: IDomEditor): string | boolean {
    return ''
  }

  /**
   * 菜单是否激活
   */
  isActive(editor: IDomEditor): boolean {
    return false
  }

  /**
   * 菜单是否禁用
   */
  isDisabled(editor: IDomEditor): boolean {
    return false
  }

  /**
   * 执行菜单命令
   */
  exec(editor: IDomEditor, value: string | boolean) {
    try {
      console.log('ImageGalleryMenu exec called', editor)
      // 调用编辑器实例上的showImageGallery方法
      const showFn = (editor as any).showImageGallery
      if (typeof showFn === 'function') {
        showFn.call(editor)
      } else {
        console.warn('showImageGallery method not found on editor')
      }
    } catch (error) {
      console.error('打开图库选择器失败:', error)
    }
  }
}

// 导出菜单类和配置
export { ImageGalleryMenu }

// 菜单配置
export const imageGalleryMenuConf = {
  key: 'imageGallery',
  factory() {
    return new ImageGalleryMenu()
  }
}

// 注册菜单
if (typeof Boot !== 'undefined') {
  console.log('Registering ImageGallery menu with key:', imageGalleryMenuConf.key)
  Boot.registerMenu(imageGalleryMenuConf)
  console.log('ImageGallery menu registered successfully')
} else {
  console.error('Boot is not available for menu registration')
}