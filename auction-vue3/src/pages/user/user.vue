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

      <!-- Tab 栏 (横向滚动) -->
      <scroll-view class="tab-bar" scroll-x :scroll-into-view="'tab-' + activeTab" :show-scrollbar="false">
        <view v-for="tab in tabs" :key="tab.key" :id="'tab-' + tab.key"
              :class="['tab-item', activeTab === tab.key && 'active']"
              @tap="switchTab(tab.key)">
          <text>{{ tab.label }}</text>
        </view>
      </scroll-view>

      <!-- 在售商品 -->
      <view v-if="activeTab === 'mine'" class="section">
        <view class="item-list">
          <view v-for="item in myItems" :key="item.id" class="list-item" @tap="goDetail(item.id)">
            <image class="list-thumb" :src="item.cover || '/static/noimage.png'" mode="aspectFill" />
            <view class="list-info">
              <text class="list-name">{{ item.title }}</text>
              <text class="list-detail">起拍价：¥{{ item.startPrice }} | 当前：¥{{ item.currentPrice }}</text>
              <text class="list-detail">状态：{{ statusMap[item.status] || item.status }}</text>
            </view>
            <button v-if="item.status === 'on_sale'" class="btn-delist" @tap.stop="handleDelist(item.id)">下架</button>
            <text v-else class="status-tag">{{ statusMap[item.status] || item.status }}</text>
          </view>
          <view v-if="myItems.length === 0" class="empty">
            <text>暂无在售商品</text>
          </view>
        </view>
      </view>

      <!-- 已下架商品 -->
      <view v-if="activeTab === 'offshelf'" class="section">
        <view class="item-list">
          <view v-for="item in offShelfItems" :key="item.id" class="list-item" @tap="goDetail(item.id)">
            <image class="list-thumb" :src="item.cover || '/static/noimage.png'" mode="aspectFill" />
            <view class="list-info">
              <text class="list-name">{{ item.title }}</text>
              <text class="list-detail">起拍价：¥{{ item.startPrice }} | 当前：¥{{ item.currentPrice }}</text>
              <text class="list-detail">状态：{{ statusMap[item.status] || item.status }}</text>
            </view>
            <view class="btn-group">
              <button class="btn-relist" @tap.stop="handleRelist(item.id)">重新上架</button>
              <button class="btn-delete" @tap.stop="handleDelete(item.id)">删除</button>
            </view>
          </view>
          <view v-if="offShelfItems.length === 0" class="empty">
            <text>暂无已下架商品</text>
          </view>
        </view>
      </view>


      <!-- 已售出 -->
      <view v-if="activeTab === 'sold'" class="section">
        <view class="item-list">
          <view v-for="item in soldItems" :key="item.id" class="list-item" @tap="goDetail(item.id)">
            <image class="list-thumb" :src="item.cover || '/static/noimage.png'" mode="aspectFill" />
            <view class="list-info">
              <text class="list-name">{{ item.title }}</text>
              <text class="list-detail">成交价：¥{{ item.currentPrice }}</text>
              <text class="list-detail">状态：已售出</text>
            </view>
          </view>
          <view v-if="soldItems.length === 0" class="empty">
            <text>暂无已售出商品</text>
          </view>
        </view>
      </view>


      <!-- 审核中 -->
      <view v-if="activeTab === 'pending'" class="section">
        <view class="item-list">
          <view v-for="item in pendingItems" :key="item.id" class="list-item">
            <image class="list-thumb" :src="item.cover || '/static/noimage.png'" mode="aspectFill" />
            <view class="list-info">
              <text class="list-name">{{ item.title }}</text>
              <text class="list-detail">起拍价：¥{{ item.startPrice }}</text>
              <text class="list-detail">状态：待管理员审核</text>
            </view>
          </view>
          <view v-if="pendingItems.length === 0" class="empty">
            <text>暂无审核中商品</text>
          </view>
        </view>
      </view>

      <!-- 我的竞拍 -->
      <view v-if="activeTab === 'bids'" class="section">
        <view class="item-list">
          <view v-for="bid in myBids" :key="bid.id" class="list-item">
            <view class="list-info full">
              <text class="list-name">{{ bid.goodsTitle }}</text>
              <text class="list-detail">出价：¥{{ bid.price }}</text>
              <text class="list-detail">时间：{{ bid.createdAt }}</text>
              <text :class="['list-status', bidStatusClass(bid)]">{{ bidStatusText(bid) }}</text>
            </view>
            <button v-if="bid.status === 'active'" class="btn-cancel-bid" @tap="handleCancelBid(bid)">取消出价</button>
          </view>
          <view v-if="myBids.length === 0" class="empty">
            <text>暂无竞拍记录</text>
          </view>
        </view>
      </view>

      <!-- 购买订单 -->
      <view v-if="activeTab === 'purchases'" class="section">
        <view class="item-list">
          <view v-for="order in purchaseOrders" :key="order.id" class="list-item" @tap="showOrderDetail(order)">
            <view class="list-info full">
              <text class="list-name">{{ order.goodsTitle }}</text>
              <text class="list-detail">成交价：¥{{ order.amount || order.price }}</text>
              <text class="list-detail">卖家：{{ order.sellerName }}</text>
              <text :class="['list-status', order.status]">{{ orderStatusText(order) }}</text>
            </view>
            <button v-if="canApplyRefund(order)" class="btn-apply-refund" @tap.stop="handleApplyRefund(order)">申请售后</button>
          </view>
          <view v-if="purchaseOrders.length === 0" class="empty">
            <text>暂无购买订单</text>
          </view>
        </view>
      </view>

      <!-- 卖出订单 -->
      <view v-if="activeTab === 'sales'" class="section">
        <view class="item-list">
          <view v-for="order in sellOrders" :key="order.id" class="list-item" @tap="showOrderDetail(order)">
            <view class="list-info full">
              <text class="list-name">{{ order.goodsTitle }}</text>
              <text class="list-detail">成交价：¥{{ order.amount || order.price }}</text>
              <text class="list-detail">买家：{{ order.buyerName }}</text>
              <text :class="['list-status', order.status]">{{ order.status }}</text>
            </view>
          </view>
          <view v-if="sellOrders.length === 0" class="empty">
            <text>暂无卖出订单</text>
          </view>
        </view>
      </view>


      <!-- 账单 -->
      <view v-if="activeTab === 'bills'" class="section">
        <view class="item-list">
          <view v-for="b in bills" :key="b.id" class="list-item">
            <view class="list-info full">
              <view class="bill-head">
                <text :class="[ 'bill-title', b.amount >= 0 ? 'bill-in' : 'bill-out' ]">{{ billTypeText(b.type) }}</text>
                <text :class="[ 'bill-amount', b.amount >= 0 ? 'bill-in' : 'bill-out' ]">{{ b.amount >= 0 ? '+' : '' }}¥{{ b.amount }}</text>
              </view>
              <text class="list-detail">说明: {{ b.memo || "-" }}</text>
              <text class="list-detail">时间: {{ formatTime(b.createdAt) }} | 余额: ¥{{ b.balanceAfter }}</text>
            </view>
          </view>
          <view v-if="bills.length === 0" class="empty">
            <text>暂无账单记录</text>
          </view>
        </view>
      </view>

      <!-- 订单详情弹窗 -->
      <view v-if="orderDetail" class="modal-overlay" @tap="closeOrderDetail">
        <view class="modal-content" @tap.stop>
          <view class="modal-title">订单详情</view>
          <view class="detail-row">
            <text>订单编号：{{ orderDetail.id }}</text>
          </view>
          <view class="detail-row">
            <text>商品名称：{{ orderDetail.goodsTitle }}</text>
          </view>
          <view class="detail-row">
            <text>成交价格：¥{{ orderDetail.amount || orderDetail.price }}</text>
          </view>
          <view v-if="orderDetail.fee" class="detail-row">
            <text>平台手续费：¥{{ orderDetail.fee }}</text>
          </view>
          <view class="detail-row">
            <text>买家：{{ orderDetail.buyerName }}</text>
          </view>
          <view class="detail-row">
            <text>卖家：{{ orderDetail.sellerName }}</text>
          </view>
          <view class="detail-row">
            <text>交易状态：已完成</text>
          </view>
          <view class="detail-row">
            <text>创建时间：{{ orderDetail.createdAt }}</text>
          </view>
          <button class="btn-close" @tap="closeOrderDetail">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { apiGetProfile, apiRecharge, apiMyGoods, apiMyBids, apiPurchases, apiSales, apiOffShelf, apiRelist, apiDeleteGoods, apiCancelBid, apiMyGoodsAll, apiBills, apiApplyRefund } from "../../utils/api";
import { getCurrentUserId, clearCurrentUser } from "../../utils/storage";

export default {
  data() {
    return {
      userId: null,
      profile: {},
      rechargeAmount: "",
      recharging: false,
      activeTab: "mine",
      tabs: [
        { key: "mine", label: "在售" },
        { key: "offshelf", label: "已下架" },
        { key: "sold", label: "已售出" },
        { key: "pending", label: "审核中" },
        { key: "bids", label: "我的竞拍" },
        { key: "purchases", label: "购买订单" },
        { key: "sales", label: "卖出订单" },
        { key: "bills", label: "账单" }
      ],
      myItems: [],
      offShelfItems: [],
      soldItems: [],
      pendingItems: [],
      myBids: [],
      purchaseOrders: [],
      sellOrders: [],
      bills: [],
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
    this.loadOffShelfItems();
    if (this.activeTab === 'bills') this.loadBills();
    if (this.activeTab === 'sold') this.loadSoldItems();
    if (this.activeTab === 'pending') this.loadPendingItems();
  },
  methods: {
    goDetail(id) { uni.navigateTo({ url: "/pages/auction/detail?id=" + id }); },
    goLogin() {
      uni.redirectTo({ url: "/pages/login/login" });
    },
    handleLogout() {
      uni.showModal({
        title: "提示",
        content: "确定退出登录吗？",
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
          apiMyGoods(),
          apiMyBids(),
          apiPurchases(),
          apiSales()
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
        async loadBills() {
      try {
        this.bills = await apiBills();
      } catch (e) {
        console.log("load bills error", e);
        this.bills = [];
      }
    },
    billTypeText(t) {
      const map = {
        recharge: "充值",
        place_bid: "出价扣款",
        bid_refund: "出价退款",
        cancel_refund: "取消退款",
        offshelf_refund: "下架退款",
        sold_income: "卖出收入",
        sold_fee: "平台手续费",
        after_sale_refund: "售后退款",
        after_sale_payback: "售后扣款"
      };
      return map[t] || t;
    },
    formatTime(t) {
      if (!t) return "";
      return String(t).replace("T", " ").substring(0, 16);
    },
switchTab(key) {
      this.activeTab = key;
      if (key === "offshelf") this.loadOffShelfItems();
      if (key === "bills") this.loadBills();
      if (key === "sold") this.loadSoldItems();
      if (key === "pending") this.loadPendingItems();
    },
    async loadOffShelfItems() {
      try {
        const items = await apiMyGoodsAll("off_shelf", "");
        this.offShelfItems = (items || []).filter(i => i.status === "off_shelf");
      } catch (e) {
        console.log("load off-shelf error", e);
      }
    },
    async loadSoldItems() {
      try {
        const items = await apiMyGoodsAll("sold", "");
        this.soldItems = (items || []).filter(i => i.status === "sold");
      } catch (e) {
        console.log("load sold error", e);
      }
    },
    async loadPendingItems() {
      try {
        const items = await apiMyGoodsAll("pending", "");
        this.pendingItems = (items || []).filter(i => i.status === "pending");
      } catch (e) {
        console.log("load pending error", e);
      }
    },
    orderStatusText(order) {
      const base = { paid: "已完成", refunded: "已退款", cancelled: "已取消" };
      const baseText = base[order.status] || order.status;
      const afterText = (() => { const m = { none: "", pending: "售后审核中", refunded: "已退款", rejected: "已被驳回" }; return m[order.afterSaleStatus] || ""; })();
      return afterText ? baseText + " | " + afterText : baseText;
    },
    canApplyRefund(order) {
      return order && order.status === "paid" && (!order.afterSaleStatus || order.afterSaleStatus === "none");
    },
    handleApplyRefund(order) {
      uni.showModal({
        title: "申请售后",
        editable: true,
        placeholderText: "请输入退款原因",
        success: async (r) => {
          if (!r.confirm) return;
          try {
            await apiApplyRefund(order.id, r.content || "");
            uni.showToast({ title: "已提交,等待管理员审核", icon: "success" });
            this.loadData();
          } catch (e) {
            uni.showToast({ title: e?.message || "提交失败", icon: "none" });
          }
        }
      });
    },
    bidStatusText(bid) {
      if (bid.status === "won") return "已中标";
      if (bid.status === "cancelled") return "已取消";
      if (bid.status === "outbid") return "已被超越";
      return bid.isLeading ? "领先" : "出局";
    },
    bidStatusClass(bid) {
      if (bid.status === "won") return "won";
      if (bid.status === "cancelled") return "outbid";
      if (bid.status === "outbid") return "outbid";
      return bid.isLeading ? "leading" : "outbid";
    },
    async handleCancelBid(bid) {
      uni.showModal({
        title: "确认取消出价",
        content: "取消后 ¥" + bid.price + " 将退还到您的余额",
        success: async (res) => {
          if (res.confirm) {
            try {
              await apiCancelBid(bid.id);
              uni.showToast({ title: "已取消,款项已退还", icon: "success" });
              this.loadData();
            } catch (e) {
              uni.showToast({ title: e?.message || "取消失败", icon: "none" });
            }
          }
        }
      });
    },
    async handleRelist(id) {
      uni.showModal({
        title: "确认重新上架",
        content: "重新上架后,价格将重置为起拍价,历史出价者的款项将退还",
        success: async (res) => {
          if (res.confirm) {
            try {
              await apiRelist(id);
              uni.showToast({ title: "已重新上架", icon: "success" });
              this.loadOffShelfItems();
            } catch (e) {
              uni.showToast({ title: e?.message || "操作失败", icon: "none" });
            }
          }
        }
      });
    },
    async handleDelete(id) {
      uni.showModal({
        title: "确认删除",
        content: "删除后商品不可恢复,所有未退款的出价将退还给买家",
        confirmText: "确认删除",
        confirmColor: "#ff4d4f",
        success: async (res) => {
          if (res.confirm) {
            try {
              await apiDeleteGoods(id);
              uni.showToast({ title: "已删除", icon: "success" });
              this.loadOffShelfItems();
            } catch (e) {
              uni.showToast({ title: e?.message || "删除失败", icon: "none" });
            }
          }
        }
      });
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
        uni.showToast({ title: e?.message || "充值失败", icon: "none" });
      } finally {
        this.recharging = false;
      }
    },
    async handleDelist(id) {
      try {
        await apiOffShelf(id);
        uni.showToast({ title: "下架成功", icon: "success" });
        this.loadData();
      } catch (e) {
        uni.showToast({ title: e?.message || "下架失败", icon: "none" });
      }
    },
    showOrderDetail(order) {
      this.orderDetail = order;
    },
    closeOrderDetail() {
      this.orderDetail = null;
    }
  }
};
</script>

<style scoped>
.user-page {
  padding: 20rpx;
  min-height: 100vh;
  background: #f5f5f5;
}
.login-tip {
  text-align: center;
  padding: 100rpx 40rpx;
}
.btn-login {
  margin-top: 40rpx;
  background: #007AFF;
  color: #fff;
  border-radius: 16rpx;
  padding: 20rpx 60rpx;
  display: inline-block;
}
.user-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #007AFF;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: bold;
  flex-shrink: 0;
}
.user-info {
  flex: 1;
  margin-left: 24rpx;
}
.nickname {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  display: block;
}
.phone {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-top: 8rpx;
}
.balance-area {
  text-align: right;
  margin-left: 20rpx;
}
.balance-label {
  font-size: 22rpx;
  color: #999;
  display: block;
}
.balance-value {
  font-size: 32rpx;
  color: #ff6b35;
  font-weight: 600;
  display: block;
}
.btn-logout {
  background: #f5f5f5;
  color: #666;
  font-size: 24rpx;
  border-radius: 8rpx;
  padding: 8rpx 20rpx;
  margin-left: 20rpx;
  border: none;
}
.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}
.recharge-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.recharge-input {
  flex: 1;
  height: 72rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  background: #fafafa;
}
.btn-recharge {
  background: #007AFF;
  color: #fff;
  border-radius: 12rpx;
  padding: 16rpx 30rpx;
  font-size: 26rpx;
  border: none;
}
.tab-bar {
  background: #fff;
  white-space: nowrap;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.tab-item {
  display: inline-block;
  text-align: center;
  padding: 24rpx 36rpx;
  font-size: 26rpx;
  color: #666;
  position: relative;
}
.tab-item.active {
  color: #007AFF;
  font-weight: 600;
}
.tab-item.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 4rpx;
  background: #007AFF;
  border-radius: 2rpx;
}
.item-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.list-item {
  display: flex;
  align-items: center;
  background: #fafafa;
  border-radius: 12rpx;
  padding: 20rpx;
  gap: 20rpx;
}
.list-thumb {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  background: #eee;
}
.list-info {
  flex: 1;
  min-width: 0;
}
.list-info.full {
  width: 100%;
}
.list-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.list-detail {
  font-size: 24rpx;
  color: #888;
  display: block;
  margin-top: 6rpx;
}
.list-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  display: inline-block;
  margin-top: 8rpx;
}
.list-status.leading {
  background: #e6f7ee;
  color: #07c160;
}
.list-status.outbid {
  background: #fff5e6;
  color: #ff8800;
}
.btn-delist {
  background: #ff4d4f;
  color: #fff;
  border-radius: 8rpx;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  border: none;
  flex-shrink: 0;
}
.status-tag.won { background: #e6f7ee; color: #07c160; }
.status-tag.outbid { background: #fff5e6; color: #ff8800; }
.btn-group { display: flex; flex-direction: column; gap: 12rpx; flex-shrink: 0; }
.btn-relist, .btn-delete, .btn-cancel-bid {
  font-size: 22rpx;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
  border: none;
  line-height: 1.4;
}
.btn-relist { background: #007AFF; color: #fff; }
.btn-delete { background: #ff4d4f; color: #fff; }
.btn-cancel-bid { background: #fff5e6; color: #ff8800; }
.btn-relist::after, .btn-delete::after, .btn-cancel-bid::after { border: none; }
.status-tag {
  font-size: 22rpx;
  color: #999;
  flex-shrink: 0;
}
.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 26rpx;
}
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.modal-content {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  width: 80%;
  max-width: 600rpx;
}
.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
  margin-bottom: 30rpx;
  color: #333;
}
.detail-row {
  font-size: 26rpx;
  color: #666;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.btn-close {
  margin-top: 30rpx;
  background: #007AFF;
  color: #fff;
  border-radius: 12rpx;
  padding: 20rpx 0;
  font-size: 28rpx;
  border: none;
  width: 100%;
}

.bill-head {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 12rpx; margin-bottom: 8rpx; border-bottom: 1rpx solid #f0f0f0;
}
.bill-title { font-size: 28rpx; font-weight: 500; }
.bill-amount { font-size: 32rpx; font-weight: 600; }
.bill-in { color: #34c759; }
.bill-out { color: #e74c3c; }


.btn-apply-refund {
  background: #fff5f0; color: #ff6b35; border: 1rpx solid #ff6b35;
  font-size: 24rpx; padding: 12rpx 24rpx; border-radius: 8rpx;
  margin-top: 12rpx; line-height: 1; height: auto;
}
.btn-apply-refund::after { border: none; }

</style>
