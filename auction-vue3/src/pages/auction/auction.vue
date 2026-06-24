<template>
  <view class="auction-page">
    <!-- 筛选与搜索 -->
    <view class="filter-bar">
      <view class="filter-row">
        <picker class="picker" :range="typeOptions" @change="onTypeChange">
          <view class="picker-value">{{ typeText }}</view>
        </picker>
        <view class="search-box">
          <input class="search-input" v-model="keyword" placeholder="搜索商品名称" @input="debounceSearch" />
          <text class="search-icon">🔍</text>
        </view>
      </view>
    </view>

    <!-- 卡片列表 -->
    <view class="card-list">
      <view v-for="(item, idx) in displayList" :key="item.id" class="card" @tap="goDetail(item)">
        <view class="card-img-wrap">
          <image class="card-img" :src="item.cover || '/static/noimage.png'" mode="aspectFill" />
          <view class="card-type">{{ item.type }}</view>
        </view>
        <view class="card-body">
          <text class="card-title">{{ item.title }}</text>
          <view class="card-price-row">
            <view class="price-block">
              <text class="price-label">起拍价</text>
              <text class="price-value">¥{{ item.startPrice }}</text>
            </view>
            <view class="price-block price-block-high">
              <text class="price-label">当前最高</text>
              <text class="price-value price-value-high">¥{{ item.currentPrice }}</text>
            </view>
          </view>
          <view class="card-footer">
            <text class="card-time">⏰ {{ formatTime(item.endAt) }}</text>
            <button class="btn-bid" @tap.stop="showBidDialog(item)">立即竞拍</button>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading"><text>加载中...</text></view>
      <view v-else-if="displayList.length === 0" class="empty-tip">
        <text>暂无符合条件的拍卖品</text>
      </view>
    </view>

    <!-- 竞拍弹窗 -->
    <view v-if="bidVisible" class="modal-overlay" @tap="closeBidDialog">
      <view class="modal-content" @tap.stop>
        <view class="modal-title">出价竞拍</view>
        <view class="bid-info">
          <text class="bid-info-label">商品</text>
          <text class="bid-info-value">{{ bidItem?.title }}</text>
        </view>
        <view class="bid-info">
          <text class="bid-info-label">当前最高价</text>
          <text class="bid-info-value bid-info-high">¥{{ bidItem?.currentPrice }}</text>
        </view>
        <view class="bid-info">
          <text class="bid-info-label">您的余额</text>
          <text class="bid-info-value">¥{{ currentBalance }}</text>
        </view>
        <input class="bid-input" v-model="bidAmount" type="digit" placeholder="请输入出价金额" />
        <view class="modal-actions">
          <button class="btn-cancel" @tap="closeBidDialog">取消</button>
          <button class="btn-confirm" @tap="confirmBid">确认出价</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { apiGoodsList, apiPlaceBid, apiGetProfile, apiGoodsDetail } from "../../utils/api";
import { getCurrentUserId } from "../../utils/storage";

const TYPE_OPTIONS = ["全部", "数码", "古董", "书画", "珠宝", "奢侈品", "收藏品"];

export default {
  data() {
    return {
      typeOptions: TYPE_OPTIONS,
      typeIndex: 0,
      keyword: "",
      displayList: [],
      allItems: [],
      bidVisible: false,
      bidItem: null,
      bidAmount: "",
      searchTimer: null,
      loading: false,
      currentBalance: 0
    };
  },
  computed: {
    typeText() { return TYPE_OPTIONS[this.typeIndex] || "全部"; }
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
    formatTime(t) {
      if (!t) return "";
      return t.replace("T", " ").substring(0, 16);
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
        uni.showToast({ title: "加载失败: " + (e?.message || ""), icon: "none" });
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
        list = list.filter(item => item.title && item.title.indexOf(kw) !== -1);
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
    goDetail(item) {
      uni.navigateTo({ url: "/pages/auction/detail?id=" + item.id });
    },
    async showBidDialog(item) {
      if (!getCurrentUserId()) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return setTimeout(() => uni.redirectTo({ url: "/pages/login/login" }), 500);
      }
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
        uni.showToast({ title: "竞拍成功", icon: "success" });
        this.closeBidDialog();
        this.loadItems();
      } catch (e) {
        if (e.message && e.message.indexOf("余额不足") !== -1) {
          uni.showModal({
            title: "余额不足",
            content: e.message + "，请先充值",
            confirmText: "去充值",
            success: (r) => { if (r.confirm) uni.switchTab({ url: "/pages/user/user" }); }
          });
        } else {
          uni.showToast({ title: e?.message || "操作失败", icon: "none" });
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
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.filter-row { display: flex; align-items: center; gap: 20rpx; }
.picker {
  flex-shrink: 0;
  background: #f0f0f0;
  padding: 14rpx 24rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
  min-width: 100rpx;
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
.card-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  display: flex;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.card:active {
  background: #f9f9f9;
}
.card-img-wrap {
  width: 220rpx;
  height: 220rpx;
  flex-shrink: 0;
  position: relative;
  background: #e0e0e0;
}
.card-img {
  width: 100%;
  height: 100%;
}
.card-type {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  background: rgba(0,122,255,0.85);
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}
.card-body {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}
.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  margin-bottom: 12rpx;
}
.card-price-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 12rpx;
}
.price-block {
  flex: 1;
  background: #f5f5f5;
  border-radius: 10rpx;
  padding: 10rpx 12rpx;
  min-width: 0;
}
.price-block-high {
  background: #fff3f0;
}
.price-label {
  font-size: 20rpx;
  color: #999;
  display: block;
  margin-bottom: 4rpx;
}
.price-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  display: block;
}
.price-value-high {
  color: #e74c3c;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}
.card-time {
  font-size: 22rpx;
  color: #999;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.btn-bid {
  flex-shrink: 0;
  font-size: 24rpx;
  background: linear-gradient(135deg, #ff6b35, #e74c3c);
  color: #fff;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  line-height: 1.4;
  border: none;
  font-weight: 500;
}
.btn-bid::after { border: none; }
.loading { text-align: center; padding: 60rpx; color: #999; font-size: 28rpx; }
.empty-tip { text-align: center; padding: 100rpx 0; color: #999; font-size: 28rpx; }
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
  color: #333;
}
.bid-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  font-size: 26rpx;
}
.bid-info-label { color: #999; }
.bid-info-value { color: #333; font-weight: 500; }
.bid-info-high { color: #e74c3c; font-weight: 600; }
.bid-input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #ddd;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 32rpx;
  box-sizing: border-box;
  margin: 24rpx 0;
}
.modal-actions { display: flex; gap: 20rpx; }
.btn-cancel {
  flex: 1; height: 76rpx; line-height: 76rpx;
  background: #f0f0f0; color: #666;
  border-radius: 12rpx; font-size: 28rpx;
}
.btn-cancel::after { border: none; }
.btn-confirm {
  flex: 1; height: 76rpx; line-height: 76rpx;
  background: linear-gradient(135deg, #ff6b35, #e74c3c); color: #fff;
  border-radius: 12rpx; font-size: 28rpx;
}
.btn-confirm::after { border: none; }
</style>
