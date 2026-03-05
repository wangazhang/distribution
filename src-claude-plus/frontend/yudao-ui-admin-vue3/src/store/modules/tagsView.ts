import router from '@/router'
import type { RouteLocationNormalizedLoaded, RouteRecordNormalized } from 'vue-router'
import { getRawRoute } from '@/utils/routerHelper'
import { defineStore } from 'pinia'
import { store } from '../index'
import { findIndex } from '@/utils'
import { useUserStoreWithOut } from './user'

export interface TagsViewState {
  visitedViews: RouteLocationNormalizedLoaded[]
  cachedViews: Set<string>
  selectedTag?: RouteLocationNormalizedLoaded
}

type PersistableRoute = Pick<
  RouteLocationNormalizedLoaded,
  'fullPath' | 'path' | 'name' | 'meta' | 'params' | 'query' | 'hash' | 'matched'
> & { title?: string }

// 为了让 tagsView 能在刷新后恢复，需要把路由对象转成可序列化的轻量结构
const normalizeRoute = (route: RouteLocationNormalizedLoaded) => {
  return Object.assign({}, getRawRoute(route)) as RouteLocationNormalizedLoaded
}

// 避免在非浏览器环境下读取 sessionStorage 报错，提供兜底实现
const createNoopStorage = (): Storage => ({
  length: 0,
  clear: () => undefined,
  key: () => null,
  getItem: () => null,
  setItem: () => undefined,
  removeItem: () => undefined
})

const getSessionStorage = (): Storage => {
  if (typeof window === 'undefined' || !window.sessionStorage) {
    return createNoopStorage()
  }
  return window.sessionStorage
}

const serializeRoute = (route?: RouteLocationNormalizedLoaded): PersistableRoute | undefined => {
  if (!route) return route
  const normalized = normalizeRoute(route)
  const { fullPath, path, name, meta, params, query, hash, matched } = normalized
  return {
    fullPath,
    path,
    name,
    meta,
    params,
    query,
    hash,
    matched,
    title: (route as PersistableRoute)?.title
  }
}

const deserializeRoute = (
  route?: PersistableRoute
): RouteLocationNormalizedLoaded | undefined => {
  if (!route) return route
  return route as RouteLocationNormalizedLoaded
}

export const useTagsViewStore = defineStore('tagsView', {
  state: (): TagsViewState => ({
    visitedViews: [],
    cachedViews: new Set(),
    selectedTag: undefined
  }),
  getters: {
    getVisitedViews(): RouteLocationNormalizedLoaded[] {
      return this.visitedViews
    },
    getCachedViews(): string[] {
      return Array.from(this.cachedViews)
    },
    getSelectedTag(): RouteLocationNormalizedLoaded | undefined {
      return this.selectedTag
    }
  },
  actions: {
    // 新增缓存和tag
    addView(view: RouteLocationNormalizedLoaded): void {
      this.addVisitedView(view)
      this.addCachedView()
    },
    // 新增tag
    addVisitedView(view: RouteLocationNormalizedLoaded) {
      if (this.visitedViews.some((v) => v.fullPath === view.fullPath)) return
      if (view.meta?.noTagsView) return
      const visitedView = Object.assign({}, normalizeRoute(view), {
        title: view.meta?.title || 'no-name'
      })

      if (visitedView.meta) {
        const titleSuffixList: string[] = []
        this.visitedViews.forEach((v) => {
          if (v.path === visitedView.path && v.meta?.title === visitedView.meta?.title) {
            titleSuffixList.push(v.meta?.titleSuffix || '1')
          }
        })
        if (titleSuffixList.length) {
          let titleSuffix = 1
          while (titleSuffixList.includes(`${titleSuffix}`)) {
            titleSuffix += 1
          }
          visitedView.meta.titleSuffix = titleSuffix === 1 ? undefined : `${titleSuffix}`
        }
      }

      this.visitedViews.push(visitedView)
    },
    // 新增缓存
    addCachedView() {
      const cacheMap: Set<string> = new Set()
      for (const v of this.visitedViews) {
        const rawRoute = getRawRoute(v)
        if (rawRoute.meta?.noCache) {
          continue
        }
        const matched = v.matched || []
        const lastMatched = matched[matched.length - 1] as RouteRecordNormalized | undefined
        const component = lastMatched?.components?.default as any
        const componentName =
          (v.meta?.cacheName as string | undefined) ||
          (lastMatched?.meta?.cacheName as string | undefined) ||
          component?.name ||
          component?.__name

        if (componentName) {
          cacheMap.add(componentName)
        }

        const routeName = rawRoute.name as string | undefined
        if (routeName) {
          cacheMap.add(routeName)
        }
      }
      if (Array.from(this.cachedViews).sort().toString() === Array.from(cacheMap).sort().toString())
        return
      this.cachedViews = cacheMap
    },
    // 删除某个
    delView(view: RouteLocationNormalizedLoaded) {
      this.delVisitedView(view)
      this.delCachedView()
    },
    // 删除tag
    delVisitedView(view: RouteLocationNormalizedLoaded) {
      for (const [i, v] of this.visitedViews.entries()) {
        if (v.fullPath === view.fullPath) {
          this.visitedViews.splice(i, 1)
          break
        }
      }
    },
    // 删除缓存
    delCachedView() {
      const route = router.currentRoute.value
      const index = findIndex<string>(this.getCachedViews, (v) => v === route.name)
      if (index > -1) {
        this.cachedViews.delete(this.getCachedViews[index])
      }
    },
    // 删除所有缓存和tag
    delAllViews() {
      this.delAllVisitedViews()
      this.delCachedView()
    },
    // 删除所有tag
    delAllVisitedViews() {
      const userStore = useUserStoreWithOut()

      // const affixTags = this.visitedViews.filter((tag) => tag.meta.affix)
      this.visitedViews = userStore.getUser
        ? this.visitedViews.filter((tag) => tag?.meta?.affix)
        : []
    },
    // 删除其他
    delOthersViews(view: RouteLocationNormalizedLoaded) {
      this.delOthersVisitedViews(view)
      this.addCachedView()
    },
    // 删除其他tag
    delOthersVisitedViews(view: RouteLocationNormalizedLoaded) {
      this.visitedViews = this.visitedViews.filter((v) => {
        return v?.meta?.affix || v.fullPath === view.fullPath
      })
    },
    // 删除左侧
    delLeftViews(view: RouteLocationNormalizedLoaded) {
      const index = findIndex<RouteLocationNormalizedLoaded>(
        this.visitedViews,
        (v) => v.fullPath === view.fullPath
      )
      if (index > -1) {
        this.visitedViews = this.visitedViews.filter((v, i) => {
          return v?.meta?.affix || v.fullPath === view.fullPath || i > index
        })
        this.addCachedView()
      }
    },
    // 删除右侧
    delRightViews(view: RouteLocationNormalizedLoaded) {
      const index = findIndex<RouteLocationNormalizedLoaded>(
        this.visitedViews,
        (v) => v.fullPath === view.fullPath
      )
      if (index > -1) {
        this.visitedViews = this.visitedViews.filter((v, i) => {
          return v?.meta?.affix || v.fullPath === view.fullPath || i < index
        })
        this.addCachedView()
      }
    },
    updateVisitedView(view: RouteLocationNormalizedLoaded) {
      for (let v of this.visitedViews) {
        if (v.fullPath === view.fullPath) {
          v = Object.assign(v, view)
          break
        }
      }
    },
    // 设置当前选中的 tag
    setSelectedTag(tag: RouteLocationNormalizedLoaded) {
      this.selectedTag = normalizeRoute(tag)
    },
    setTitle(title: string, path?: string) {
      for (const v of this.visitedViews) {
        if (v.path === (path ?? this.selectedTag?.path)) {
          v.meta.title = title
          break
        }
      }
    }
  },
  // 保持 tagsView 在整页刷新后的状态
  persist: {
    key: 'tags-view',
    storage: getSessionStorage(),
    paths: ['visitedViews', 'cachedViews', 'selectedTag'],
    serializer: {
      serialize: (state: TagsViewState) => {
        const payload = {
          visitedViews: state.visitedViews
            .map((item) => serializeRoute(item))
            .filter((item): item is PersistableRoute => Boolean(item)),
          cachedViews: Array.from(state.cachedViews),
          selectedTag: serializeRoute(state.selectedTag)
        }
        return JSON.stringify(payload)
      },
      deserialize: (value: string): TagsViewState => {
        try {
          const parsed = JSON.parse(value)
          return {
            visitedViews: (parsed?.visitedViews || [])
              .map((item: PersistableRoute) => deserializeRoute(item))
              .filter((item): item is RouteLocationNormalizedLoaded => Boolean(item)),
            cachedViews: new Set(parsed?.cachedViews || []),
            selectedTag: deserializeRoute(parsed?.selectedTag)
          }
        } catch (error) {
          console.error('[tagsView persist] deserialize error =>', error)
          return {
            visitedViews: [],
            cachedViews: new Set(),
            selectedTag: undefined
          }
        }
      }
    }
  }
})

export const useTagsViewStoreWithOut = () => {
  return useTagsViewStore(store)
}
