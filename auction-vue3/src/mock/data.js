/**
 * mock/data.js — 初始 Mock 数据
 * 首次运行时写入 localStorage
 */

const MOCK_USERS = [
  { id: 1, username: 'alice', phone: '13800000001', password: '123456', nickname: 'Alice', balance: 10000 },
  { id: 2, username: 'bob', phone: '13800000002', password: '123456', nickname: 'Bob', balance: 5000 },
  { id: 3, username: 'admin', phone: '13800000000', password: '123456', nickname: '管理员', balance: 99999 }
];

const now = Date.now();
const pad = n => String(n).padStart(2, '0');
const fmt = (d) => {
  const dt = new Date(d);
  return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}:${pad(dt.getSeconds())}`;
};
const addDays = (d, days) => d + days * 86400000;

const MOCK_ITEMS = [
  { id: 1001, name: 'iPhone 15 Pro Max', type: '数码', description: '苹果最新旗舰手机，256G 原色钛金属，99新，配件齐全，无拆无修。支持全网通5G。', startingPrice: 6000, currentMaxPrice: 7800, endTime: fmt(addDays(now, 3)), sellerId: 2, sellerName: 'Bob', image: '', status: 'active', createdAt: fmt(now - 86400000) },
  { id: 1002, name: '清代康熙青花瓷瓶', type: '古董', description: '康熙年间景德镇官窑青花瓷瓶，品相完好，底部有官窑款识，高约30cm。附权威鉴定证书。', startingPrice: 20000, currentMaxPrice: 35000, endTime: fmt(addDays(now, 5)), sellerId: 1, sellerName: 'Alice', image: '', status: 'active', createdAt: fmt(now - 72000000) },
  { id: 1003, name: '百事可乐限定罐', type: '收藏品', description: '2024年春节限定版百事可乐空罐，全新未开封，限量发行5000罐，收藏价值高。', startingPrice: 50, currentMaxPrice: 120, endTime: fmt(addDays(now, 1)), sellerId: 3, sellerName: '管理员', image: '', status: 'active', createdAt: fmt(now - 36000000) },
  { id: 1004, name: '齐白石《虾》仿制画', type: '书画', description: '高仿真宣纸印刷，画芯尺寸68×45cm，带实木画框，非常适合家居装饰。', startingPrice: 200, currentMaxPrice: 350, endTime: fmt(addDays(now, 7)), sellerId: 2, sellerName: 'Bob', image: '', status: 'active', createdAt: fmt(now - 43200000) },
  { id: 1005, name: '和田玉籽料手串', type: '珠宝', description: '天然和田玉籽料手串，每颗直径10mm，油润细腻，附带国家珠宝检测证书。', startingPrice: 3000, currentMaxPrice: 5200, endTime: fmt(addDays(now, 4)), sellerId: 1, sellerName: 'Alice', image: '', status: 'active', createdAt: fmt(now - 21600000) },
  { id: 1006, name: 'LV Neverfull 中号手袋', type: '奢侈品', description: '经典老花图案，中号尺寸，9成新，肩带轻微使用痕迹，附防尘袋和购买小票复印件。', startingPrice: 5000, currentMaxPrice: 6800, endTime: fmt(addDays(now, 2)), sellerId: 3, sellerName: '管理员', image: '', status: 'active', createdAt: fmt(now - 10800000) },
  { id: 1007, name: '可口可乐复古玻璃瓶套装', type: '收藏品', description: '6瓶装复古玻璃瓶可口可乐，含1940s-1990s经典瓶型复刻版，未开封，展示收藏佳品。', startingPrice: 80, currentMaxPrice: 150, endTime: fmt(addDays(now, 6)), sellerId: 2, sellerName: 'Bob', image: '', status: 'active', createdAt: fmt(now - 5400000) },
  { id: 1008, name: '索尼 WH-1000XM5 耳机', type: '数码', description: '索尼旗舰降噪耳机，铂金银色，使用1个月，箱说全，成色几乎全新。', startingPrice: 1500, currentMaxPrice: 1800, endTime: fmt(addDays(now, 3)), sellerId: 1, sellerName: 'Alice', image: '', status: 'active', createdAt: fmt(now - 6000000) },
  { id: 1009, name: '张大千《荷花》立轴', type: '书画', description: '高精度博物馆级复制画，绢本立轴，尺寸120×60cm，装裱精美，极具观赏价值。', startingPrice: 500, currentMaxPrice: 800, endTime: fmt(addDays(now, 8)), sellerId: 3, sellerName: '管理员', image: '', status: 'active', createdAt: fmt(now - 3000000) },
  { id: 1010, name: '卡地亚 Tank 腕表', type: '奢侈品', description: '卡地亚 Tank Francaise 系列，钢带石英款，7成新，走时精准，表镜有微小划痕。', startingPrice: 8000, currentMaxPrice: 10500, endTime: fmt(addDays(now, 4)), sellerId: 2, sellerName: 'Bob', image: '', status: 'active', createdAt: fmt(now - 1500000) },
  { id: 1011, name: '冰种翡翠吊坠', type: '珠宝', description: '天然A货冰种翡翠，阳绿色，雕工精细，配18K金项链，附权威鉴定证书。', startingPrice: 12000, currentMaxPrice: 16800, endTime: fmt(addDays(now, 10)), sellerId: 1, sellerName: 'Alice', image: '', status: 'active', createdAt: fmt(now - 800000) },
  { id: 1012, name: '明代铜鎏金佛像', type: '古董', description: '明代铜鎏金释迦牟尼坐像，高约20cm，鎏金部分脱落，铜绿自然，包浆厚重。', startingPrice: 15000, currentMaxPrice: 22000, endTime: fmt(addDays(now, 6)), sellerId: 3, sellerName: '管理员', image: '', status: 'active', createdAt: fmt(now - 200000) },
  { id: 1013, name: 'MacBook Pro 14\u0044 M3', type: '数码', description: 'Apple M3芯片，18G内存，512G固态，深空灰色，使用3个月，电池循环20次。', startingPrice: 10000, currentMaxPrice: 12500, endTime: fmt(addDays(now, 5)), sellerId: 2, sellerName: 'Bob', image: '', status: 'active', createdAt: fmt(now - 100000) },
  { id: 1014, name: '可口可乐 X 故宫联名礼盒', type: '收藏品', description: '可口可乐与故宫联名限定礼盒，含6罐定制可乐+故宫文创周边，全新未拆封。', startingPrice: 120, currentMaxPrice: 200, endTime: fmt(addDays(now, 2)), sellerId: 1, sellerName: 'Alice', image: '', status: 'active', createdAt: fmt(now - 50000) }
];

export function initMockData() {
  try {
    const hasInit = uni.getStorageSync('_mock_inited_');
    if (hasInit) return;
    uni.setStorageSync('user_list', MOCK_USERS);
    uni.setStorageSync('auction_items', MOCK_ITEMS);
    uni.setStorageSync('bid_records', []);
    uni.setStorageSync('orders', []);
    uni.setStorageSync('_mock_inited_', true);
    uni.setStorageSync('next_user_id', 4);
    uni.setStorageSync('next_item_id', 1015);
    uni.setStorageSync('next_bid_id', 1);
    uni.setStorageSync('next_order_id', 1);
    console.log('[mock] initial data written');
  } catch (e) {
    console.error('[mock] init fail', e);
  }
}
