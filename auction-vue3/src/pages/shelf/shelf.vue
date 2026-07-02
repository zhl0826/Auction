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
        <view class="picker-wrap" @tap="showTypeModal = true">
          <view :class="['picker-text', typeIndex < 0 && 'placeholder']">{{ typeOptions[typeIndex] || "请选择类型" }}</view>
        </view>
      </view>

      <!-- 自定义类型选择弹层 -->
      <view v-if="showTypeModal" class="modal-mask" @tap="showTypeModal = false">
        <view class="modal-card" @tap.stop="noop">
          <view class="modal-title">选择商品类型</view>
          <view
            v-for="(opt, idx) in typeOptions"
            :key="idx"
            class="modal-item"
            :class="idx === typeIndex ? 'active' : ''"
            @tap="pickType(idx)"
          >{{ opt }}</view>
          <view class="modal-cancel" @tap="showTypeModal = false">取消</view>
        </view>
      </view>

      <view class="form-item">
        <text class="label">商品描述（详细信息）</text>
        <textarea class="textarea" v-model="form.description" placeholder="请详细描述商品状况、来源等" maxlength="500" />
      </view>

      <!-- AI 描述生成 -->
      <view class="form-item" style="margin-top: -10rpx;">
        <view class="ai-row">
          <button class="btn-ai" :disabled="!canAiGenerate" :loading="aiLoading" @tap="handleAiGenerate">
            <text v-if="aiLoading">AI 生成中...</text>
            <text v-else>{{ form.description.trim() ? "AI ✨ 润色" : "AI ✨ 生成介绍词" }}</text>
          </button>
        </view>
      </view>

      <view class="form-item">
        <text class="label">最低起拍价（元）<text class="required">*</text></text>
        <input class="input" v-model="form.startPrice" type="digit" placeholder="请输入起拍价格" />
      <view class="ai-valuation-row">
        <button class="btn-valuation" :disabled="!canAiValuation || valuationLoading" @tap="handleAiValuation">
          <text v-if="valuationLoading">AI 估价中...</text>
          <text v-else>🤖 AI 智能估价</text>
        </button>
      </view>
      </view>

      <view class="form-item">
        <text class="label">竞拍截止时间 <text class="required">*</text></text>
        <picker class="input picker-elem" mode="date" :value="endDate" @change="onDateChange">
          <view :class="['picker-text', !endDate && 'placeholder']">{{ endDate || "请选择截止日期" }}</view>
        </picker>
      </view>

      <view class="form-item">
        <picker class="input picker-elem" mode="time" :value="endTime" @change="onTimeChange">
          <view :class="['picker-text', !endTime && 'placeholder']">{{ endTime || "请选择截止时间" }}</view>
        </picker>
      </view>

      <button class="btn-primary" :disabled="submitting" @tap="handleSubmit">{{ submitting ? "提交中..." : "提交上架" }}</button>
    </view>

    <!-- AI 估价结果弹窗 -->
    <view class="modal-overlay" v-if="showValuationModal" @tap="closeValuationModal">
      <view class="modal-card" @tap.stop>
        <view class="modal-title">🤖 AI 估价建议</view>
        <scroll-view class="chat-box" scroll-y :scroll-into-view="'chat-bottom'">
          <view v-for="(item, idx) in valuationChatHistory" :key="idx" :class="['chat-msg', item.role === 'ai' ? 'msg-ai' : 'msg-user']">
            <text class="msg-text">{{ item.text }}</text>
          </view>
          <view id="chat-bottom" style="height:1rpx"></view>
        </scroll-view>
        <view class="chat-input-row">
          <input class="chat-input" v-model="valuationChatMsg" placeholder="追问估价细节..." @confirm="handleValuationChat" />
          <button class="chat-send" :disabled="!valuationChatMsg.trim()" @tap="handleValuationChat">发送</button>
        </view>
        <button class="btn-close" @tap="closeValuationModal">关闭</button>
      </view>
    </view>
  </view>
</template>

<script>
import { apiAddGoods, apiUploadImage, apiAiDescription, apiAiValuation, apiAiValuationChat } from "../../utils/api";
import { getCurrentUserId } from "../../utils/storage";
const TYPE_OPTIONS = ["数码", "古董", "书画", "珠宝", "奢侈品", "收藏品"];
export default {
  data() {
    return {
      typeOptions: TYPE_OPTIONS,
      images: [],
      form: { title: "", type: "", description: "", startPrice: "" },
      typeIndex: -1,
      endDate: "",
      endTime: "",
      submitting: false,
      aiLoading: false,
      valuationLoading: false,
      valuationResult: "",
      valuationSessionId: "",
      valuationChatHistory: [],
      showValuationModal: false,
      valuationChatMsg: "",
      showTypeModal: false
    };
  },
  computed: {
    hasStartPrice() {
      const v = parseFloat(this.form.startPrice);
      return !isNaN(v) && v > 0;
    },
    canAiValuation() {
      // 估价只需标题+类型, 起拍价作为参考传给 AI 即可 (没有也不阻塞)
      return this.form.title.trim() && this.form.type;
    },
    canAiGenerate() {
      // 只需要标题和类型, 价格可选 (没填则 AI 不在文案里提价格)
      return this.form.title.trim() && this.form.type;
    }
  },
  onShow() {
    if (!getCurrentUserId()) {
      uni.showToast({ title: "请先登录", icon: "none" });
      setTimeout(() => uni.redirectTo({ url: "/pages/login/login" }), 500);
    }
  },
  watch: {
    typeIndex(v) { console.log('[shelf] typeIndex ->', v); },
    'form.type'(v) { console.log('[shelf] form.type ->', v); }
  },
  methods: {
    chooseImage() {
      uni.chooseImage({ count: 9, sizeType: ["compressed"], sourceType: ["album", "camera"],
        success: (res) => { this.images = res.tempFilePaths || []; }, fail: () => {} });
    },
    onTypeChange(e) { this.typeIndex = e.detail.value; this.form.type = this.typeOptions[e.detail.value]; },
    pickType(idx) {
      this.typeIndex = idx;
      this.form.type = this.typeOptions[idx];
      this.showTypeModal = false;
    },
    noop() {},
    onDateChange(e) { this.endDate = e.detail.value; },
    onTimeChange(e) { this.endTime = e.detail.value; },
    async handleAiGenerate() {
      if (this.aiLoading) return;
      this.aiLoading = true;
      try {
        const r = await apiAiDescription({
          title: this.form.title.trim(),
          type: this.form.type,
          startPrice: this.hasStartPrice ? parseFloat(this.form.startPrice) : null,
          description: this.form.description.trim() || ""
        });
        this.form.description = r || "";
        uni.showToast({ title: "AI 生成完成，可编辑后发布", icon: "success" });
      } catch (e) {
        uni.showToast({ title: e?.message || "AI 生成失败", icon: "none" });
      } finally { this.aiLoading = false; }
    },
    async handleAiValuation() {
      if (this.valuationLoading) return;
      this.valuationLoading = true;
      this.valuationResult = "";
      this.valuationSessionId = "";
      this.valuationChatHistory = [];
      try {
        const r = await apiAiValuation({
          title: this.form.title.trim(),
          type: this.form.type,
          startPrice: this.hasStartPrice ? parseFloat(this.form.startPrice) : null,
          description: this.form.description.trim() || ""
        });
        // Parse session ID from result
        const sessionMatch = r.match(/【会话ID: ([^】]+)】/);
        if (sessionMatch) {
          this.valuationSessionId = sessionMatch[1];
          this.valuationResult = r.replace(/【会话ID: [^】]+】\n?/, "");
        } else {
          this.valuationResult = r;
        }
        this.valuationChatHistory.push({ role: "ai", text: this.valuationResult });
        this.showValuationModal = true;
      } catch (e) {
        uni.showToast({ title: e?.message || "AI 估价失败", icon: "none" });
      } finally { this.valuationLoading = false; }
    },
    async handleValuationChat() {
      const msg = this.valuationChatMsg.trim();
      if (!msg) return;
      if (!this.valuationSessionId) {
        return uni.showToast({ title: "会话已过期，请重新估价", icon: "none" });
      }
      this.valuationChatHistory.push({ role: "user", text: msg });
      this.valuationChatMsg = "";
      try {
        const r = await apiAiValuationChat(this.valuationSessionId, msg);
        this.valuationChatHistory.push({ role: "ai", text: r });
      } catch (e) {
        uni.showToast({ title: e?.message || "回复失败", icon: "none" });
      }
    },
    closeValuationModal() {
      this.showValuationModal = false;
      this.valuationChatHistory = [];
      this.valuationSessionId = "";
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
        let coverUrl = "";
        if (this.images.length > 0) {
          const upRes = await apiUploadImage(this.images[0]);
          coverUrl = upRes.fullUrl;
        }
        await apiAddGoods({
          title: this.form.title.trim(),
          type: this.form.type,
          description: this.form.description.trim() || "",
          startPrice: price,
          cover: coverUrl,
          endAt: this.endDate + " " + this.endTime + ":00"
        });
        uni.showToast({ title: "上架成功！等待管理员审核", icon: "success" });
        this.form = { title: "", type: "", description: "", startPrice: "" };
        this.images = []; this.endDate = ""; this.endTime = "";
        setTimeout(() => uni.switchTab({ url: "/pages/auction/auction" }), 800);
      } catch (e) {
        uni.showToast({ title: e?.message || "上架失败", icon: "none" });
      } finally { this.submitting = false; }
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

.picker-wrap { line-height: 80rpx; padding: 0 24rpx; font-size: 28rpx; color: #333; background: #fafafa; border: 2rpx solid #e0e0e0; border-radius: 12rpx; box-sizing: border-box; width: 100%; height: 80rpx; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: flex-end; justify-content: center; }
.modal-card { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 32rpx 24rpx; max-height: 80vh; }
.modal-title { font-size: 32rpx; font-weight: bold; text-align: center; margin-bottom: 24rpx; color: #333; }
.modal-item { padding: 28rpx 0; text-align: center; font-size: 30rpx; color: #333; border-bottom: 1rpx solid #eee; }
.modal-item.active { color: #ff5722; font-weight: bold; background: #fff5f0; }
.modal-cancel { padding: 28rpx 0; text-align: center; font-size: 30rpx; color: #999; margin-top: 16rpx; }
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
.picker-value.placeholder,
.picker-text.placeholder {
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
.ai-row { display: flex; justify-content: center; }
.btn-ai {
  width: 100%; height: 76rpx; line-height: 76rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; font-size: 28rpx; border-radius: 38rpx;
  border: none; text-align: center;
}
.btn-ai::after { border: none; }
.btn-ai[disabled] { opacity: 0.4; }
.ai-valuation-row { display: flex; justify-content: center; margin-top: -10rpx; }
.btn-valuation {
  width: 100%; height: 72rpx; line-height: 72rpx;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: #fff; font-size: 28rpx; border-radius: 36rpx;
  border: none; text-align: center;
}
.btn-valuation::after { border: none; }
.btn-valuation[disabled] { opacity: 0.4; }

/* 估价弹窗 */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: center; justify-content: center;
  padding: 40rpx;
}
.modal-card {
  width: 100%; max-width: 650rpx; max-height: 80vh;
  background: #fff; border-radius: 24rpx; padding: 30rpx;
  display: flex; flex-direction: column;
}
.modal-title {
  font-size: 34rpx; font-weight: 600; color: #333;
  text-align: center; margin-bottom: 20rpx;
}
.chat-box {
  flex: 1; max-height: 500rpx;
  background: #f9f9f9; border-radius: 16rpx;
  padding: 20rpx; margin-bottom: 16rpx;
}
.chat-msg { margin-bottom: 16rpx; }
.msg-ai { text-align: left; }
.msg-user { text-align: right; }
.msg-text {
  display: inline-block; max-width: 80%;
  padding: 14rpx 20rpx; border-radius: 12rpx;
  font-size: 26rpx; line-height: 1.6;
  text-align: left; word-break: break-all;
}
.msg-ai .msg-text { background: #e8f4ff; color: #333; }
.msg-user .msg-text { background: #007AFF; color: #fff; }
.chat-input-row {
  display: flex; align-items: center; gap: 16rpx;
  margin-bottom: 16rpx;
}
.chat-input {
  flex: 1; height: 68rpx; border: 2rpx solid #e0e0e0;
  border-radius: 12rpx; padding: 0 20rpx; font-size: 26rpx;
  background: #fafafa; box-sizing: border-box;
}
.chat-send {
  height: 68rpx; line-height: 68rpx; padding: 0 30rpx;
  background: #007AFF; color: #fff; font-size: 26rpx;
  border-radius: 12rpx; border: none; flex-shrink: 0;
}
.chat-send::after { border: none; }
.chat-send[disabled] { opacity: 0.5; }
.btn-close {
  width: 100%; height: 72rpx; line-height: 72rpx;
  background: #f5f5f5; color: #666; font-size: 28rpx;
  border-radius: 16rpx; border: none;
}
.btn-close::after { border: none; }
</style>