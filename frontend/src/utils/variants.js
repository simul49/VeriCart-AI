// Helpers for product variant / SKU option rendering.
//
// `variants` is a JSON string stored on the product, shaped as:
//   [ { "name": "Color", "options": ["Black", "White"] },
//     { "name": "Size",  "options": ["S", "M", "L"] } ]
//
// A Chinese → English map is applied so any legacy Chinese labels still
// render in English (older DB seeds or rows pre-migration).

const VARIANT_I18N = {
  // Group names
  '颜色': 'Color', '尺码': 'Size', '存储': 'Storage', '屏幕尺寸': 'Screen Size',
  '规格': 'Specification', '色号': 'Shade', '适配': 'Compatibility',
  '型号': 'Model', '口味': 'Flavor',
  // Color options
  '黑色': 'Black', '白色': 'White', '灰色': 'Gray', '红色': 'Red', '蓝色': 'Blue',
  '深空灰': 'Space Gray', '银色': 'Silver', '金色': 'Gold', '米色': 'Beige',
  '原木色': 'Natural Wood', '自然色': 'Natural', '偏白色': 'Fair',
  '浅粉': 'Light Pink', '浅蓝': 'Light Blue', '米白': 'Cream', '多色': 'Multicolor',
  // Spec / packaging
  '标准装': 'Standard', '大容量': 'Large', '基础款': 'Basic', '旗舰款': 'Premium',
  '通用款': 'Universal', '专车专用': 'Model Specific', '单本': 'Single', '套装': 'Set',
  '小包装': 'Small', '大包装': 'Large',
  // Flavors (pets)
  '鸡肉': 'Chicken', '牛肉': 'Beef', '三文鱼': 'Salmon'
}

const translate = (v) => (v == null ? v : (VARIANT_I18N[v] ?? v))

export function parseVariants(product) {
  const raw = product && product.variants
  if (!raw) return []
  try {
    const v = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (!Array.isArray(v)) return []
    return v
      .filter(g => g && Array.isArray(g.options) && g.options.length)
      .map(g => ({ name: translate(g.name), options: g.options.map(translate) }))
  } catch {
    return []
  }
}

// Returns a { groupName: firstOption } map so a product starts with a default selection.
export function defaultSelection(groups) {
  const sel = {}
  groups.forEach(g => {
    if (g.options && g.options.length) sel[g.name] = g.options[0]
  })
  return sel
}

export function selectionSummary(selected) {
  if (!selected) return ''
  return Object.entries(selected)
    .filter(([, v]) => v)
    .map(([k, v]) => `${k} ${v}`)
    .join(' / ')
}