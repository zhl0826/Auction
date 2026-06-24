<template>
  <view class="shelf-page">
    <view class="form-card">
      <view class="form-title">上架拍卖品</view>

      <view class="form-item">
        <text class="label">商品图片（最多9张）</text>
        <view class="upload-area">
          <image v-if="images[0]" class="preview" :src="images[0]" mode="aspectFill" @tap="chooseImage" />
          <view v-else class="upload-placeholder" @tap="chooseImage">
            <text class="upload-icon">+</text>
            <text class="upload-text">点击拍摄/选择</text>
          </view>
          <text v-if="images.length > 0" class="hint">已选 {{ images.length }} 张，展示第一张</text>
        </view>
      </view>

      <view class="form-item">
        <text class="label">商品名称 <text class="required">*</text></text>
        <input class="input" v-model="form.title" placeholder="请输入商品名称" maxlength="50" />
      </view>

      <view class="form-item">
        <text class="label">商品类型 <text class="required">*</text></text>
        <picker class="input picker-elem" :range="typeOptions" @change="onTypeChange">
          <view :class="['picker-value', !form.type && 'placeholder']">{{ form.type || "请选择类型" }}</view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">商品描述（详细信息）</text>
        <textarea class="textarea" v-model="form.description" placeholder="请详细描述商品状况、来源等" maxlength="500" />
      </view>

      <view class="form-item">
        <text class="label">最低起拍价（元）<text class="required">*</text></text>
        <input class="input" v-model="form.startPrice" type="digit" placeholder="请输入起拍价格" />
      </view>

      <view class="form-item">
        <text class="label">竞拍截止时间 <text class="required">*</text></text>
        <picker class="input picker-elem" mode="date" :value="endDate" @change="onDateChange">
          <view :class="['picker-value', !endDate && 'placeholder']">{{ endDate || "请选择截止日期" }}</view>
        </picker>
      </view>

      <view class="form-item">
        <picker class="input picker-elem" mode="time" :value="endTime" @change="onTimeChange">
          <view :class="['picker-value', !endTime && 'placeholder']">{{ endTime || "请选择截止时间" }}</view>
        </picker>
      </view>

      <button class="btn-primary" :disabled="submitting" @tap="handleSubmit">{{ submitting ? "提交中..." : "提交上架" }}</button>
    </view>
  </view>
</template>

<script>
import { apiAddGoods, apiUploadImage } from "../../utils/api";
import { getCurrentUserId } from "../../utils/storage";

const TYPE_OPTIONS = ["数码", "古董", "书画", "珠宝", "奢侈品", "收藏品"];

export default {
  data() {
    return {
      typeOptions: TYPE_OPTIONS,
      images: [],
      form: { title: "", type: "", description: "", startPrice: "" },
      endDate: "",
      endTime: "",
      submitting: false
    };
  },
  onShow() {
    if (!getCurrentUserId()) {
      uni.showToast({ title: "请先登录", icon: "none" });
      setTimeout(() => uni.redirectTo({ url: "/pages/login/login" }), 500);
    }
  },
  methods: {
    chooseImage() {
      uni.chooseImage({
        count: 9,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          this.images = res.tempFilePaths || [];
        },
        fail: () => {}
      });
    },
    onTypeChange(e) {
      this.form.type = this.typeOptions[e.detail.value];
    },
    onDateChange(e) {
      this.endDate = e.detail.value;
    },
    onTimeChange(e) {
      this.endTime = e.detail.value;
    },
    async handleSubmit() {
      if (!getCurrentUserId()) return uni.showToast({ title: "请先登录", icon: "none" });
      if (!this.form.title.trim()) return uni.showToast({ title: "请输入商品名称", icon: "none" });
      if (!this.form.type) return uni.showToast({ title: "请选择商品类型", icon: "none" });
      const price = parseFloat(this.form.startPrice);
      if (isNaN(price) || price <= 0) return uni.showToast({ title: "请输入有效起拍价", icon: "none" });
      if (!this.endDate || !this.endTime) return uni.showToast({ title: "请选择竞拍截止时间", icon: "none" });
      this.submitting = true;
      try {
        // 先上传图片 (如果有)
        let coverUrl = "";
        if (this.images.length > 0) {
          const upRes = await apiUploadImage(this.images[0]);
          coverUrl = upRes.fullUrl;
        }
        const goods = {
          title: this.form.title.trim(),
          type: this.form.type,
          description: this.form.description.trim() || "",
          startPrice: price,
          cover: coverUrl,
          endAt: this.endDate + " " + this.endTime + ":00"
        };
        await apiAddGoods(goods);
        uni.showToast({ title: "上架成功！等待管理员审核", icon: "success" });
        this.form = { title: "", type: "", description: "", startPrice: "" };
        this.images = [];
        this.endDate = "";
        this.endTime = "";
        setTimeout(() => uni.switchTab({ url: "/pages/auction/auction" }), 800);
      } catch (e) {
        uni.showToast({ title: e?.message || "上架失败", icon: "none" });
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.shelf-page {
  padding: 20rpx;
  min-height: 100vh;
  background: #f5f5f5;
}
.form-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.form-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 40rpx;
}
.form-item {
  margin-bottom: 30rpx;
}
.label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}
.required {
  color: #ff4d4f;
}
.input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  background: #fafafa;
  box-sizing: border-box;
}
.picker-elem {
  height: 80rpx;
  padding: 0;
  line-height: 80rpx;
}
.picker-value {
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333;
}
.picker-value.placeholder {
  color: #999;
}
.textarea {
  width: 100%;
  min-height: 200rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  background: #fafafa;
  box-sizing: border-box;
}
.upload-area {
  width: 100%;
  height: 240rpx;
  border: 2rpx dashed #ccc;
  border-radius: 12rpx;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.preview {
  width: 100%;
  height: 100%;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}
.upload-icon {
  font-size: 60rpx;
  color: #999;
}
.upload-text {
  font-size: 24rpx;
  color: #999;
}
.hint {
  position: absolute;
  bottom: 12rpx;
  right: 16rpx;
  font-size: 22rpx;
  color: #fff;
  background: rgba(0,0,0,0.5);
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}
.btn-primary {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #007AFF, #0056D6);
  color: #fff;
  font-size: 32rpx;
  border-radius: 16rpx;
  margin-top: 40rpx;
  border: none;
}
.btn-primary::after { border: none; }
.btn-primary[disabled] { opacity: 0.6; }
</style>
