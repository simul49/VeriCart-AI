import { createI18n } from 'vue-i18n'
import en from './locales/en'
import zh from './locales/zh'

const STORAGE_KEY = 'vericart-locale'
export const SUPPORTED = [
  { code: 'en', label: 'English', short: 'EN' },
  { code: 'zh', label: '中文', short: '中' }
]

/** Saved choice wins; otherwise follow the browser language. */
function detectLocale() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved === 'en' || saved === 'zh') return saved
  } catch { /* localStorage unavailable (private mode) */ }
  const nav = (navigator.language || 'en').toLowerCase()
  return nav.startsWith('zh') ? 'zh' : 'en'
}

function applyHtmlLang(locale) {
  document.documentElement.lang = locale === 'zh' ? 'zh-CN' : 'en'
}

const initial = detectLocale()
applyHtmlLang(initial)

const i18n = createI18n({
  legacy: false,          // Composition API mode
  locale: initial,
  fallbackLocale: 'en',
  globalInjection: true,  // enables {{ $t('...') }} directly in templates
  messages: { en, zh }
})

/** Switch language and remember the choice. */
export function setLocale(locale) {
  if (locale !== 'en' && locale !== 'zh') return
  i18n.global.locale.value = locale
  applyHtmlLang(locale)
  try {
    localStorage.setItem(STORAGE_KEY, locale)
  } catch { /* ignore */ }
}

/** Current locale as a plain string. */
export function currentLocale() {
  return i18n.global.locale.value
}

export default i18n
