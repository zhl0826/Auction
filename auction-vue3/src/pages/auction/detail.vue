<template>
  <view class="detail-page">
    <view v-if="loading" class="loading"><text>加载中...</text></view>

    <block v-else-if="goods">
      <image class="hero" :src="goods.cover || '/static/noimage.png'" mode="aspectFill" />

      <view class="card">
        <view class="title-row">
          <text class="type-tag">{{ goods.type }}</text>
        </view>
        <text class="title">{{ goods.title }}</text>
        <view class="price-row">
          <view class="price-item">
            <text class="price-label">起拍价</text>
            <text class="price-val">¥{{ goods.startPrice }}</text>
          </view>
          <view class="price-item price-item-high">
            <text class="price-label">当前最高</text>
            <text class="price-val price-val-high">¥{{ goods.currentPrice }}</text>
          </view>
        </view>
        <view class="time-row">
          <text class="time-icon">⏰</text>
          <text class="time-text">结束时间: {{ formatTime(goods.endAt) }}</text>
        </view>
      </view>

      <view class="card">
        <view class="section-title">商品描述</view>
        <text class="desc-text">{{ goods.description || "卖家很懒,没有写描述" }}</text>
      </view>

      <view class="card">
        <view class="section-title">卖家信息</view>
        <view class="seller-row">
          <view class="avatar">{{ (goods.sellerName || "U").charAt(0).toUpperCase() }}</view>
          <view class="seller-info">
            <text class="seller-name">{{ goods.sellerName }}</text>
            <text class="seller-id">卖家 ID: {{ goods.sellerId }}</text>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="section-title">拍卖规则</view>
        <view class="rule-item">📈 最低加价幅度: ¥{{ goods.minIncrement }}</view>
        <view class="rule-item">💰 您的余额: ¥{{ currentBalance }}</view>
        <view class="rule-item">⚠️ 出价后无法撤销,请谨慎出价</view>
      </view>

      <view v-if="isSeller && goods.status === 'on_sale'" class="card">
        <view class="section-title">卖家操作</view>
        <button class="btn-close" :disabled="closing" @tap="handleCloseAuction">
          {{ closing ? "处理中..." : (goods.currentBidderId ? "立即成交 (¥" + goods.currentPrice + ")" : "下架商品 (无人出价)") }}
        </button>
      </view>

      <view class="card">
        <view class="section-title">出价历史 ({{ bids.length }})</view>
        <view v-if="bids.length === 0" class="empty-tip-inline">
          <text>暂无出价</text>
        </view>
        <view v-else class="bid-list">
          <view v-for="(b, idx) in bids" :key="b.id" class="bid-row">
            <view class="bid-rank">#{{ idx + 1 }}</view>
            <view class="bid-main">
              <text class="bid-user">{{ b.bidder || "用户" + b.bidderId }}</text>
              <text class="bid-time">{{ formatTime(b.createdAt) }}</text>
            </view>
            <view class="bid-side">
              <text class="bid-price">¥{{ b.price }}</text>
              <text :class="['bid-status', 'bs-' + b.status]">{{ bidStatusText(b) }}</text>
            </view>
          </view>
        </view>
      </view>

      <view style="height: 200rpx;"></view>
    </block>

    <view v-else class="empty-tip"><text>商品不存在或已下架</text></view>

    <!-- 底部操作栏 -->
    <view v-if="goods" class="bottom-bar">
      <view class="bottom-info">
        <text class="bottom-info-label">{{ isMyBid ? '我已领先' : '当前最高' }}</text>
        <text class="bottom-info-value">¥{{ goods.currentPrice }}</text>
      </view>
      <button class="btn-bid" :class="{ 'btn-bid-mine': isMyBid }" @tap="showBidDialog">
        {{ isMyBid ? '继续加价' : '立即出价' }}
      </button>
    </view>

    <!-- 竞拍弹窗 -->
    <view v-if="bidVisible" class="modal-overlay" @tap="closeBidDialog">
      <view class="modal-content" @tap.stop>
        <view class="modal-title">出价竞拍</view>
        <view class="bid-info">
          <text class="bid-info-label">商品</text>
          <text class="bid-info-value">{{ goods?.title }}</text>
        </view>
        <view class="bid-info">
          <text class="bid-info-label">当前最高价</text>
          <text class="bid-info-value bid-info-high">¥{{ goods?.currentPrice }}</text>
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
import { apiGoodsDetail, apiPlaceBid, apiGetProfile, apiGoodsBids, apiCloseAuction, apiOffShelf } from "../../utils/api";
import { getCurrentUserId } from "../../utils/storage";

export default {
  data() {
    return {
      id: null,
      goods: null,
      loading: true,
      isMyBid: false,
      bidVisible: false,
      bidAmount: "",
      currentBalance: 0,
      currentUserId: null,
      bids: [],
      closing: false
    };
  },
  onLoad(query) {
    this.id = query.id;
    this.loadDetail();
  },
  onShow() {
    if (this.goods) this.loadDetail();
  },
  methods: {
    formatTime(t) {
      if (!t) return "";
      return t.replace("T", " ").substring(0, 16);
    },
    async loadDetail() {
      this.loading = true;
      try {
        this.goods = await apiGoodsDetail(this.id);
        try {
          const profile = await apiGetProfile();
          this.currentBalance = profile.balance || 0;
          this.isMyBid = this.goods && this.goods.currentBidderId === profile.id;
          this.currentUserId = profile.id;
          this.isSeller = this.goods && this.goods.sellerId === profile.id;
        } catch (e) {
          this.currentBalance = 0;
        }
        this.loadBids();
      } catch (e) {
        uni.showToast({ title: e?.message || "加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
      async loadBids() {
        try {
          this.bids = await apiGoodsBids(this.id);
        } catch (e) {
          this.bids = [];
        }
    },
    showBidDialog() {
      if (!getCurrentUserId()) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return setTimeout(() => uni.redirectTo({ url: "/pages/login/login" }), 500);
      }
      this.bidAmount = "";
      this.bidVisible = true;
    },
    closeBidDialog() {
      this.bidVisible = false;
    },
    bidStatusText(b) {
      const map = { active: "领先", outbid: "出局", won: "已中标", cancelled: "已取消" };
      return map[b.status] || b.status;
    },
        handleCloseAuction() {
      const g = this.goods;
      if (!g) return;
      const hasBidder = !!g.currentBidderId;
      const content = hasBidder
        ? "将以当前最高价 ¥" + g.currentPrice + " 成交给 " + (g.currentBidderName || "买家" + g.currentBidderId) + "，其他买家将自动退款，确认成交？"
        : "该商品目前无人出价，将直接下架商品。是否继续？";
      const title = hasBidder ? "确认成交" : "确认下架";
      uni.showModal({
        title,
        content,
        success: async (r) => {
          if (!r.confirm) return;
          this.closing = true;
          try {
            if (hasBidder) {
              await apiCloseAuction(this.id);
              uni.showToast({ title: "成交成功", icon: "success" });
            } else {
              await apiOffShelf(this.id);
              uni.showToast({ title: "已下架", icon: "success" });
            }
            this.loadDetail();
          } catch (e) {
            uni.showToast({ title: e?.message || "操作失败", icon: "none" });
          } finally {
            this.closing = false;
          }
        }
      });
    },
async confirmBid() {
      const amount = parseFloat(this.bidAmount);
      if (isNaN(amount) || amount <= 0) return uni.showToast({ title: "请输入有效金额", icon: "none" });
      if (amount <= this.goods.currentPrice) return uni.showToast({ title: "出价必须高于当前最高价", icon: "none" });

      try {
        await apiPlaceBid(this.id, amount);
        uni.showToast({ title: this.isMyBid ? "加价成功" : "出价成功", icon: "success" });
        this.bidVisible = false;
        this.loadDetail();
      } catch (e) {
        if (e.message && e.message.indexOf("余额不足") !== -1) {
          uni.showModal({
            title: "余额不足",
            content: e.message + "，请先充值",
            confirmText: "去充值",
            success: (r) => { if (r.confirm) uni.switchTab({ url: "/pages/user/user" }); }
          });
        } else {
          uni.showToast({ title: e?.message || "出价失败", icon: "none" });
        }
      }
    }
  }
};
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 140rpx;
}
.hero {
  width: 100%;
  height: 600rpx;
  background: #e0e0e0;
  display: block;
}
.card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.title-row {
  margin-bottom: 12rpx;
}
.type-tag {
  display: inline-block;
  background: #e8f0fe;
  color: #007AFF;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}
.title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  display: block;
  line-height: 1.4;
  margin-bottom: 24rpx;
}
.price-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}
.price-item {
  flex: 1;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 20rpx;
  text-align: center;
}
.price-item-high {
  background: #fff3f0;
}
.price-label {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-bottom: 8rpx;
}
.price-val {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  display: block;
}
.price-val-high {
  color: #e74c3c;
}
.time-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 0;
  border-top: 1rpx solid #f0f0f0;
}
.time-icon { font-size: 28rpx; }
.time-text {
  font-size: 26rpx;
  color: #666;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}
.desc-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.7;
  display: block;
  white-space: pre-wrap;
}
.seller-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #007AFF, #0056D6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: bold;
  flex-shrink: 0;
}
.seller-info {
  flex: 1;
}
.seller-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  display: block;
  margin-bottom: 6rpx;
}
.seller-id {
  font-size: 22rpx;
  color: #999;
  display: block;
}
.rule-item {
  font-size: 26rpx;
  color: #666;
  line-height: 2;
  display: block;
}
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
.bottom-info {
  flex: 1;
}
.bottom-info-label {
  font-size: 22rpx;
  color: #999;
  display: block;
}
.bottom-info-value {
  font-size: 36rpx;
  font-weight: 600;
  color: #e74c3c;
  display: block;
}
.btn-bid {
  background: linear-gradient(135deg, #ff6b35, #e74c3c);
  color: #fff;
  font-size: 30rpx;
  padding: 24rpx 60rpx;
  border-radius: 50rpx;
  font-weight: 600;
  border: none;
  line-height: 1;
}
.btn-bid-mine {
  background: linear-gradient(135deg, #ff9500, #ff6b00) !important;
}
.btn-bid::after { border: none; }
.loading { text-align: center; padding: 100rpx 0; color: #999; font-size: 28rpx; }
.empty-tip { text-align: center; padding: 200rpx 0; color: #999; font-size: 28rpx; }
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
.btn-close {
  background: linear-gradient(135deg, #34c759, #28a745);
  color: #fff;
  font-size: 30rpx;
  padding: 24rpx;
  border-radius: 12rpx;
  font-weight: 600;
  border: none;
  line-height: 1;
  width: 100%;
}
.btn-close::after { border: none; }
.btn-close[disabled] { opacity: 0.6; }

.bid-list { display: flex; flex-direction: column; gap: 16rpx; }
.bid-row {
  display: flex; align-items: center; gap: 20rpx;
  padding: 20rpx; background: #fafafa; border-radius: 12rpx;
}
.bid-rank {
  width: 60rpx; height: 60rpx; line-height: 60rpx;
  text-align: center; border-radius: 50%;
  background: #e8f0fe; color: #007AFF;
  font-size: 26rpx; font-weight: 600; flex-shrink: 0;
}
.bid-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6rpx; }
.bid-user { font-size: 28rpx; color: #333; font-weight: 500; }
.bid-time { font-size: 22rpx; color: #999; }
.bid-side { display: flex; flex-direction: column; align-items: flex-end; gap: 6rpx; }
.bid-price { font-size: 30rpx; color: #e74c3c; font-weight: 600; }
.bid-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 6rpx; }
.bs-active { background: #fff3f0; color: #e74c3c; }
.bs-outbid { background: #f0f0f0; color: #999; }
.bs-won { background: #e8f8ee; color: #34c759; font-weight: 600; }
.bs-cancelled { background: #f0f0f0; color: #999; }
.empty-tip-inline { text-align: center; color: #999; font-size: 26rpx; padding: 30rpx 0; }

</style>
