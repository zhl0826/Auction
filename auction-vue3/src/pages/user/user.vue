<template>
  <view class="user-page">
    <view v-if="!userId" class="login-tip">
      <text>请先登录</text>
      <button class="btn-login" @tap="goLogin">去登录</button>
    </view>

    <view v-else>
      <!-- 用户信息卡片 -->
      <view class="user-card">
        <view class="avatar">{{ (profile.nickname || "U").charAt(0).toUpperCase() }}</view>
        <view class="user-info">
          <text class="nickname">{{ profile.nickname || "用户" + userId }}</text>
          <text class="phone">{{ profile.phone || "" }}</text>
        </view>
        <view class="balance-area">
          <text class="balance-label">余额</text>
          <text class="balance-value">¥{{ profile.balance || 0 }}</text>
        </view>
        <button class="btn-logout" @tap="handleLogout">退出登录</button>
      </view>

      <!-- 充值 -->
      <view class="section recharge-section">
        <text class="section-title">余额充值</text>
        <view class="recharge-row">
          <input class="recharge-input" v-model="rechargeAmount" type="digit" placeholder="输入充值金额" />
          <button class="btn-recharge" :disabled="recharging" @tap="handleRecharge">{{ recharging ? "充值中..." : "充值" }}</button>
        </view>
      </view>

      <!-- Tab 切换 -->
      <view class="tab-bar">
        <view class="tab-item" v-for="tab in tabs" :key="tab.key"
          :class="{ active: activeTab === tab.key }" @tap="switchTab(tab.key)">
          <text>{{ tab.label }}</text>
        </view>
      </view>

      <!-- 我的上架 -->
      <view class="section" v-if="activeTab === 'mine'">
        <view class="item-list">
          <view class="list-item" v-for="item in myItems" :key="item.id">
            <image class="list-thumb" :src="item.cover || '/static/noimage.png'" mode="aspectFill" />
            <view class="list-info">
              <text class="list-name">{{ item.title }}</text>
              <text class="list-detail">起拍价：¥{{ item.startPrice }} | 当前：¥{{ item.currentPrice }}</text>
              <text class="list-detail">状态：{{ getStatusText(item) }}</text>
            </view>
            <button v-if="item.status === 'on_sale' || item.status === 'pending'" class="btn-delist" @tap="delistItem(item)">下架</button>
            <text v-else class="status-tag">{{ statusMap[item.status] || item.status }}</text>
          </view>
          <view class="empty" v-if="myItems.length === 0"><text>暂无上架商品</text></view>
        </view>
      </view>

      <!-- 我的竞拍 -->
      <view class="section" v-if="activeTab === 'bids'">
        <view class="item-list">
          <view class="list-item" v-for="bid in myBids" :key="bid.id">
            <view class="list-info full">
              <text class="list-name">{{ bid.goodsTitle || bid.goodsId }}</text>
              <text class="list-detail">出价：¥{{ bid.price }}</text>
              <text class="list-detail">时间：{{ bid.createdAt }}</text>
              <text class="list-status" :class="bid.isLeading ? 'leading' : 'outbid'">
                {{ bid.isLeading ? "领先" : "出局" }}
              </text>
            </view>
          </view>
          <view class="empty" v-if="myBids.length === 0"><text>暂无竞拍记录</text></view>
        </view>
      </view>

      <!-- 购买订单 -->
      <view class="section" v-if="activeTab === 'purchases'">
        <view class="item-list">
          <view class="list-item" v-for="order in purchaseOrders" :key="order.id" @tap="showOrderDetail(order)">
            <view class="list-info full">
              <text class="list-name">{{ order.goodsTitle }}</text>
              <text class="list-detail">成交价：¥{{ order.amount }}</text>
              <text class="list-detail">卖家：{{ order.sellerName }}</text>
              <text class="list-status" :class="order.status === 'completed' ? 'completed' : 'pending'">
                {{ order.status === 'completed' ? "已完成" : "已完成" }}
              </text>
            </view>
          </view>
          <view class="empty" v-if="purchaseOrders.length === 0"><text>暂无购买订单</text></view>
        </view>
      </view>

      <!-- 卖出订单 -->
      <view class="section" v-if="activeTab === 'sales'">
        <view class="item-list">
          <view class="list-item" v-for="order in sellOrders" :key="order.id" @tap="showOrderDetail(order)">
            <view class="list-info full">
              <text class="list-name">{{ order.goodsTitle }}</text>
              <text class="list-detail">成交价：¥{{ order.amount }}</text>
              <text class="list-detail">买家：{{ order.buyerName }}</text>
              <text class="list-status" :class="order.status === 'completed' ? 'completed' : 'pending'">
                {{ order.status === 'completed' ? "已完成" : "已完成" }}
              </text>
            </view>
          </view>
          <view class="empty" v-if="sellOrders.length === 0"><text>暂无卖出订单</text></view>
        </view>
      </view>
    </view>

    <!-- 订单详情弹窗 -->
    <view class="modal-overlay" v-if="orderDetail" @tap="orderDetail = null">
      <view class="modal-content" @tap.stop>
        <view class="modal-title">订单详情</view>
        <view class="detail-row"><text>订单编号：{{ orderDetail.id }}</text></view>
        <view class="detail-row"><text>商品名称：{{ orderDetail.goodsTitle }}</text></view>
        <view class="detail-row"><text>成交价格：¥{{ orderDetail.amount }}</text></view>
        <view class="detail-row"><text>买家：{{ orderDetail.buyerName }}</text></view>
        <view class="detail-row"><text>卖家：{{ orderDetail.sellerName }}</text></view>
        <view class="detail-row"><text>交易状态：已完成</text></view>
        <view class="detail-row"><text>创建时间：{{ orderDetail.createdAt }}</text></view>
        <button class="btn-close" @tap="orderDetail = null">关闭</button>
      </view>
    </view>
  </view>
</template>

<script>
import { apiGetProfile, apiRecharge, apiMyGoods, apiOffShelf, apiMyBids, apiPurchases, apiSales, apiOrderDetail } from "../../utils/api";
import { getCurrentUserId, clearCurrentUser, getCurrentNickname } from "../../utils/storage";

export default {
  data() {
    return {
      userId: null,
      profile: {},
      rechargeAmount: "",
      recharging: false,
      activeTab: "mine",
      tabs: [
        { key: "mine", label: "我的上架" },
        { key: "bids", label: "我的竞拍" },
        { key: "purchases", label: "购买订单" },
        { key: "sales", label: "卖出订单" }
      ],
      myItems: [],
      myBids: [],
      purchaseOrders: [],
      sellOrders: [],
      orderDetail: null,
      statusMap: {
        "pending": "待审核",
        "on_sale": "竞拍中",
        "sold": "已售出",
        "off_shelf": "已下架",
        "rejected": "审核未通过"
      }
    };
  },
  onShow() {
    this.userId = getCurrentUserId();
    if (!this.userId) {
      uni.showToast({ title: "请先登录", icon: "none" });
      return;
    }
    this.loadProfile();
    this.loadData();
  },
  methods: {
    goLogin() { uni.redirectTo({ url: "/pages/login/login" }); },
    handleLogout() {
      uni.showModal({
        title: "提示", content: "确定退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            clearCurrentUser();
            this.userId = null;
            this.profile = {};
            uni.showToast({ title: "已退出", icon: "none" });
          }
        }
      });
    },
    async loadProfile() {
      try {
        this.profile = await apiGetProfile();
      } catch (e) {
        console.log("load profile error", e);
      }
    },
    async loadData() {
      try {
        const [items, bids, purchases, sales] = await Promise.all([
          apiMyGoods(), apiMyBids(), apiPurchases(), apiSales()
        ]);
        this.myItems = items || [];
        this.myBids = (bids || []).map(b => {
          const item = this.myItems.find(i => i.id === b.goodsId);
          return { ...b, isLeading: item ? b.price >= item.currentPrice : false };
        });
        this.purchaseOrders = purchases || [];
        this.sellOrders = sales || [];
      } catch (e) {
        console.log("load data error", e);
      }
    },
    async switchTab(key) {
      this.activeTab = key;
      this.loadData();
    },
    async handleRecharge() {
      const amount = parseFloat(this.rechargeAmount);
      if (isNaN(amount) || amount <= 0) return uni.showToast({ title: "请输入有效金额", icon: "none" });
      this.recharging = true;
      try {
        const updated = await apiRecharge(amount);
        this.profile = updated;
        uni.showToast({ title: "充值成功", icon: "success" });
        this.rechargeAmount = "";
      } catch (e) {
        uni.showToast({ title: e.message, icon: "none" });
      } finally {
        this.recharging = false;
      }
    },
    getStatusText(item) {
      if (item.status === "on_sale") {
        const end = new Date(item.endAt.replace(" ", "T")).getTime();
        return end > Date.now() ? "竞拍中" : "已结束";
      }
      return this.statusMap[item.status] || item.status;
    },
    async delistItem(item) {
      uni.showModal({
        title: "确认下架",
        content: '确定下架商品「' + item.title + '」吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await apiOffShelf(item.id);
              uni.showToast({ title: "下架成功", icon: "success" });
              this.myItems = await apiMyGoods();
            } catch (e) {
              uni.showToast({ title: e.message, icon: "none" });
            }
          }
        }
      });
    },
    async showOrderDetail(order) {
      try {
        this.orderDetail = await apiOrderDetail(order.id);
      } catch (e) {
        this.orderDetail = order;
      }
    }
  }
};
</script>

<style scoped>
.user-page { padding: 30rpx; background: #f5f5f5; min-height: 100vh; }
.login-tip {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  min-height: 60vh; font-size: 32rpx; color: #999;
}
.btn-login {
  margin-top: 30rpx; background: #007AFF; color: #fff;
  padding: 16rpx 60rpx; border-radius: 16rpx; font-size: 28rpx;
}
.btn-login::after { border: none; }
.user-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 24rpx; padding: 40rpx;
  margin-bottom: 24rpx; position: relative; color: #fff;
}
.avatar {
  width: 100rpx; height: 100rpx; border-radius: 50%;
  background: rgba(255,255,255,0.3);
  display: flex; align-items: center; justify-content: center;
  font-size: 44rpx; font-weight: 600; margin-bottom: 16rpx;
}
.user-info { margin-bottom: 16rpx; }
.nickname { font-size: 34rpx; font-weight: 600; display: block; }
.phone { font-size: 24rpx; opacity: 0.8; display: block; margin-top: 6rpx; }
.balance-area { display: flex; align-items: baseline; gap: 12rpx; }
.balance-label { font-size: 26rpx; opacity: 0.9; }
.balance-value { font-size: 40rpx; font-weight: 700; }
.btn-logout {
  position: absolute; top: 24rpx; right: 24rpx;
  background: rgba(255,255,255,0.2); color: #fff;
  font-size: 22rpx; padding: 8rpx 24rpx; border-radius: 10rpx;
  border: none; line-height: 2;
}
.btn-logout::after { border: none; }
.recharge-section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.section-title { font-size: 28rpx; font-weight: 600; color: #333; display: block; margin-bottom: 16rpx; }
.recharge-row { display: flex; gap: 16rpx; align-items: center; }
.recharge-input {
  flex: 1; height: 68rpx; border: 2rpx solid #e0e0e0;
  border-radius: 12rpx; padding: 0 16rpx; font-size: 28rpx;
  background: #fafafa; box-sizing: border-box;
}
.btn-recharge {
  background: #27ae60; color: #fff; padding: 12rpx 36rpx;
  border-radius: 12rpx; font-size: 26rpx; flex-shrink: 0;
  border: none; line-height: 2;
}
.btn-recharge::after { border: none; }
.btn-recharge[disabled] { opacity: 0.6; }
.tab-bar {
  display: flex; background: #fff; border-radius: 12rpx;
  overflow: hidden; margin-bottom: 20rpx;
}
.tab-item {
  flex: 1; text-align: center; padding: 20rpx 0;
  font-size: 26rpx; color: #666;
  border-bottom: 4rpx solid transparent;
}
.tab-item.active { color: #007AFF; border-bottom-color: #007AFF; font-weight: 600; }
.section { margin-bottom: 20rpx; }
.item-list { background: #fff; border-radius: 16rpx; overflow: hidden; }
.list-item {
  display: flex; align-items: center; padding: 24rpx;
  border-bottom: 2rpx solid #f0f0f0; gap: 16rpx;
}
.list-item:last-child { border-bottom: none; }
.list-thumb {
  width: 100rpx; height: 100rpx; border-radius: 10rpx;
  background: #e0e0e0; flex-shrink: 0;
}
.list-info { flex: 1; min-width: 0; }
.list-info.full { flex: 1; }
.list-name {
  font-size: 28rpx; font-weight: 500; color: #333;
  display: block; margin-bottom: 6rpx;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.list-detail { font-size: 24rpx; color: #888; display: block; line-height: 1.6; }
.list-status {
  font-size: 24rpx; font-weight: 600; display: inline-block;
  margin-top: 6rpx; padding: 4rpx 16rpx; border-radius: 8rpx;
}
.list-status.leading { color: #27ae60; background: #e8f8e8; }
.list-status.outbid { color: #e74c3c; background: #fde8e8; }
.list-status.completed { color: #27ae60; background: #e8f8e8; }
.list-status.pending { color: #f39c12; background: #fef9e7; }
.btn-delist {
  background: #e74c3c; color: #fff; padding: 8rpx 24rpx;
  border-radius: 10rpx; font-size: 24rpx; border: none; flex-shrink: 0; line-height: 2;
}
.btn-delist::after { border: none; }
.status-tag {
  font-size: 24rpx; color: #999; background: #f0f0f0;
  padding: 8rpx 20rpx; border-radius: 10rpx; flex-shrink: 0;
}
.empty { padding: 60rpx; text-align: center; color: #bbb; font-size: 26rpx; }
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: center; justify-content: center;
}
.modal-content { width: 600rpx; background: #fff; border-radius: 24rpx; padding: 40rpx; }
.modal-title { font-size: 34rpx; font-weight: 600; text-align: center; margin-bottom: 32rpx; }
.detail-row { margin-bottom: 16rpx; font-size: 26rpx; color: #555; }
.btn-close {
  width: 100%; height: 72rpx; line-height: 72rpx;
  background: #f0f0f0; color: #666; border-radius: 12rpx;
  font-size: 28rpx; margin-top: 24rpx; border: none; text-align: center;
}
.btn-close::after { border: none; }
</style>
