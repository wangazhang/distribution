/**
 * CMS图标映射配置
 * 将语义化的图标名称映射到uni-icons图标类型
 */
export const CMS_ICONS = {
  // 浏览/查看
  view: 'eye',

  // 点赞
  like: 'heart',
  liked: 'heart-filled',

  // 收藏
  collect: 'star',
  collected: 'star-filled',

  // 分享
  share: 'redo',

  // 下载
  download: 'download',

  // 评论
  comment: 'chat',
  'comment-filled': 'chat-filled',

  // 用户
  person: 'person',
  people: 'person-filled',

  // 时间
  time: 'clock',

  // 位置
  location: 'location',
  'location-filled': 'location-filled',

  // 锁定/权限
  locked: 'locked',
  unlocked: 'unlocked',

  // 其他
  play: 'play-filled',
  more: 'more-filled',
  close: 'closeempty',
  search: 'search',
  refresh: 'refreshempty',
};

/**
 * 获取图标类型
 * @param {string} name - 图标名称
 * @returns {string} - uni-icons类型
 */
export function getIconType(name) {
  return CMS_ICONS[name] || name;
}

export default CMS_ICONS;
