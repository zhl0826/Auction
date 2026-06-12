<template>
  <view class="auction-page">
    <!-- 筛选与搜索栏 -->
    <view class="filter-bar">
      <view class="filter-row">
        <picker class="picker" :range="typeOptions" @change="onTypeChange">
          <view class="picker-value">{{ typeText }}</view>
        </picker>
        <view class="search-box">
          <input class="search-input" v-model="keyword" placeholder="搜索商品名称/描述" @input="debounceSearch" />
          <text class="search-icon">🔍</text>
        </view>
      </view>
    </view>

    <!-- 表格表头 -->
    <view class="table-header">
      <view class="th col-index">#</view>
      <view class="th col-img">图片</view>
      <view class="th col-name">名称</view>
      <view class="th col-type">类型</view>
      <view class="th col-price sortable" @tap="sortBy('price')">
        起拍价 <text class="sort-arrow">{{ sortPriceArrow }}</text>
      </view>
      <view class="th col-maxprice">当前最高</view>
      <view class="th col-time sortable" @tap="sortBy('time')">
        结束时间 <text class="sort-arrow">{{ sortTimeArrow }}</text>
      </view>
      <view class="th col-seller">卖家</view>
      <view class="th col-action">操作</view>
    </view>

    <!-- 商品列表 -->
    <scroll-view class="item-list" scroll-y>
      <view class="item-card" v-for="(item, idx) in displayList" :key="item.id">
        <view class="col-index">{{ idx + 1 }}</view>
        <view class="col-img">
          <image class="thumb" :src="item.cover || '/static/noimage.png'" mode="aspectFill"
            @mouseenter="showDesc(item)" @mouseleave="hideDesc" />
          <view class="desc-popup" v-if="hoverItem && hoverItem.id === item.id">
            <text class="desc-text">{{ item.description }}</text>
          </view>
        </view>
        <view class="col-name">{{ item.title }}</view>
        <view class="col-type"><text class="type-tag">{{ item.type }}</text></view>
        <view class="col-price">{{ item.startPrice }}</view>
        <view class="col-maxprice price-high">{{ item.currentPrice }}</view>
        <view class="col-time">{{ item.endAt }}</view>
        <view class="col-seller">{{ item.sellerName }}</view>
        <view class="col-action">
          <button class="btn-bid" @tap="showBidDialog(item)">立即竞拍</button>
        </view>
      </view>
      <view class="loading" v-if="loading"><text>加载中...</text></view>
      <view class="empty-tip" v-else-if="displayList.length === 0">
        <text>暂无符合条件的拍卖品</text>
      </view>
    </scroll-view>

    <!-- 竞拍弹窗 -->
    <view class="modal-overlay" v-if="bidVisible" @tap="closeBidDialog">
      <view class="modal-content" @tap.stop>
        <view class="modal-title">出价竞拍</view>
        <view class="bid-info">
          <text>商品：{{ bidItem?.title }}</text>
          <text>当前最高价：¥{{ bidItem?.currentPrice }}</text>
          <text>您的余额：¥{{ currentBalance }}</text>
        </view>
        <input class="bid-input" v-model="bidAmount" type="number" placeholder="请输入出价金额" />
        <view class="modal-actions">
          <button class="btn-cancel" @tap="closeBidDialog">取消</button>
          <button class="btn-confirm" @tap="confirmBid">确认出价</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { apiGoodsList, apiPlaceBid, apiGetProfile } from "../../utils/api";
import { getCurrentUserId } from "../../utils/storage";

const TYPE_OPTIONS = ["全部", "数码", "古董", "书画", "珠宝", "奢侈品", "收藏品"];

export default {
  data() {
    return {
      typeOptions: TYPE_OPTIONS,
      typeIndex: 0,
      keyword: "",
      sortField: "",
      sortOrder: 1,
      displayList: [],
      allItems: [],
      bidVisible: false,
      bidItem: null,
      bidAmount: "",
      hoverItem: null,
      searchTimer: null,
      loading: false,
      currentBalance: 0
    };
  },
  computed: {
    typeText() { return TYPE_OPTIONS[this.typeIndex] || "全部"; },
    sortPriceArrow() { return this.sortField !== "price" ? "⇅" : this.sortOrder === 1 ? "↑" : "↓"; },
    sortTimeArrow() { return this.sortField !== "time" ? "⇅" : this.sortOrder === 1 ? "↑" : "↓"; }
  },
  onShow() {
    this.checkLogin();
    this.loadItems();
  },
  methods: {
    checkLogin() {
      if (!getCurrentUserId()) {
        uni.showToast({ title: "请先登录", icon: "none" });
        setTimeout(() => uni.redirectTo({ url: "/pages/login/login" }), 500);
      }
    },
    async loadItems() {
      this.loading = true;
      try {
        const items = await apiGoodsList();
        const now = Date.now();
        this.allItems = items.filter(item => {
          if (item.status !== "on_sale") return false;
          const end = new Date(item.endAt.replace(" ", "T")).getTime();
          return end > now;
        });
        this.applyFilter();
      } catch (e) {
        uni.showToast({ title: "加载失败: " + e.message, icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    applyFilter() {
      let list = [...this.allItems];
      if (this.typeIndex > 0) {
        list = list.filter(item => item.type === TYPE_OPTIONS[this.typeIndex]);
      }
      if (this.keyword.trim()) {
        const kw = this.keyword.trim();
        list = list.filter(item => {
          return (item.title && item.title.indexOf(kw) !== -1) ||
                 (item.description && item.description.indexOf(kw) !== -1);
        });
      }
      if (this.sortField === "price") {
        list.sort((a, b) => this.sortOrder * (a.startPrice - b.startPrice));
      } else if (this.sortField === "time") {
        list.sort((a, b) => this.sortOrder * (new Date(a.endAt) - new Date(b.endAt)));
      }
      this.displayList = list;
    },
    debounceSearch() {
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => this.applyFilter(), 300);
    },
    onTypeChange(e) {
      this.typeIndex = parseInt(e.detail.value);
      this.applyFilter();
    },
    sortBy(field) {
      if (this.sortField === field) {
        this.sortOrder = -this.sortOrder;
      } else {
        this.sortField = field;
        this.sortOrder = field === "price" ? 1 : -1;
      }
      this.applyFilter();
    },
    showDesc(item) { this.hoverItem = item; },
    hideDesc() { this.hoverItem = null; },

    async showBidDialog(item) {
      if (!getCurrentUserId()) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return setTimeout(() => uni.redirectTo({ url: "/pages/login/login" }), 500);
      }
      // 获取当前余额
      try {
        const profile = await apiGetProfile();
        this.currentBalance = profile.balance || 0;
      } catch (e) {
        this.currentBalance = 0;
      }
      this.bidItem = item;
      this.bidAmount = "";
      this.bidVisible = true;
    },
    closeBidDialog() {
      this.bidVisible = false;
      this.bidItem = null;
      this.bidAmount = "";
    },
    async confirmBid() {
      if (!getCurrentUserId()) return uni.showToast({ title: "请先登录", icon: "none" });
      const amount = parseFloat(this.bidAmount);
      if (isNaN(amount) || amount <= 0) return uni.showToast({ title: "请输入有效金额", icon: "none" });
      if (amount <= this.bidItem.currentPrice) return uni.showToast({ title: "出价必须高于当前最高价", icon: "none" });

      try {
        await apiPlaceBid(this.bidItem.id, amount);
        uni.showToast({ title: "竞拍成功！", icon: "success" });
        this.closeBidDialog();
        this.loadItems();
      } catch (e) {
        if (e.message.indexOf("余额不足") !== -1) {
          uni.showModal({
            title: "余额不足",
            content: e.message + "，请先充值。",
            confirmText: "去充值",
            success: (r) => { if (r.confirm) uni.switchTab({ url: "/pages/user/user" }); }
          });
        } else {
          uni.showToast({ title: e.message, icon: "none" });
        }
      }
    }
  }
};
</script>

<style scoped>
.auction-page {
  padding: 20rpx;
  min-height: 100vh;
  background: #f5f5f5;
}
.filter-bar {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.filter-row { display: flex; align-items: center; gap: 20rpx; }
.picker {
  flex-shrink: 0;
  background: #f0f0f0;
  padding: 16rpx 24rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
  min-width: 120rpx;
  text-align: center;
}
.picker-value { color: #333; }
.search-box { flex: 1; position: relative; }
.search-input {
  width: 100%;
  height: 64rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  padding: 0 32rpx 0 64rpx;
  font-size: 26rpx;
  box-sizing: border-box;
}
.search-icon {
  position: absolute;
  left: 20rpx; top: 50%;
  transform: translateY(-50%);
  font-size: 28rpx;
}
.table-header {
  display: flex;
  background: #fff;
  padding: 20rpx 16rpx;
  border-radius: 12rpx 12rpx 0 0;
  font-size: 24rpx;
  font-weight: 600;
  color: #666;
  border-bottom: 2rpx solid #eee;
}
.th { text-align: center; }
.th.sortable { color: #007AFF; cursor: pointer; }
.sort-arrow { font-size: 20rpx; margin-left: 4rpx; }
.col-index { width: 5%; }
.col-img { width: 10%; }
.col-name { width: 14%; }
.col-type { width: 8%; }
.col-price { width: 10%; }
.col-maxprice { width: 10%; }
.col-time { width: 18%; }
.col-seller { width: 10%; }
.col-action { width: 15%; }
.item-list { max-height: calc(100vh - 320rpx); }
.item-card {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 16rpx;
  border-bottom: 2rpx solid #f0f0f0;
  font-size: 24rpx;
  color: #333;
}
.item-card:last-child { border-radius: 0 0 12rpx 12rpx; }
.thumb {
  width: 70rpx; height: 70rpx;
  border-radius: 8rpx;
  background: #e0e0e0;
}
.col-img { position: relative; }
.desc-popup {
  position: absolute;
  left: 100%; top: -20rpx;
  z-index: 100;
  width: 360rpx;
  background: #333;
  color: #fff;
  padding: 16rpx;
  border-radius: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.2);
  word-break: break-all;
}
.desc-text { color: #fff; }
.type-tag {
  background: #e8f0fe;
  color: #007AFF;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.price-high { color: #e74c3c; font-weight: 600; }
.btn-bid {
  font-size: 22rpx;
  background: linear-gradient(135deg, #ff6b35, #e74c3c);
  color: #fff;
  padding: 8rpx 20rpx;
  border-radius: 10rpx;
  line-height: 1.8;
  border: none;
  white-space: nowrap;
}
.btn-bid::after { border: none; }
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}
.modal-title {
  font-size: 34rpx;
  font-weight: 600;
  text-align: center;
  margin-bottom: 32rpx;
}
.bid-info { margin-bottom: 24rpx; }
.bid-info text {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
}
.bid-input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #ddd;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 32rpx;
  box-sizing: border-box;
  margin-bottom: 32rpx;
}
.modal-actions { display: flex; gap: 20rpx; }
.btn-cancel {
  flex: 1; height: 76rpx; line-height: 76rpx;
  background: #f0f0f0; color: #666;
  border-radius: 12rpx; font-size: 28rpx; text-align: center;
}
.btn-cancel::after { border: none; }
.btn-confirm {
  flex: 1; height: 76rpx; line-height: 76rpx;
  background: #007AFF; color: #fff;
  border-radius: 12rpx; font-size: 28rpx; text-align: center;
}
.btn-confirm::after { border: none; }
.loading { text-align: center; padding: 60rpx; color: #999; }
.empty-tip { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
</style>
