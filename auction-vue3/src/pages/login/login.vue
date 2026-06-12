<template>
  <view class="login-page">
    <!-- 登录表单 -->
    <view class="form-card" v-if="!isRegister">
      <view class="form-title">登录</view>
      <view class="form-item">
        <text class="label">用户名 / 手机号</text>
        <input class="input" v-model="loginForm.account" placeholder="请输入用户名或手机号" />
      </view>
      <view class="form-item">
        <text class="label">密码</text>
        <input class="input" v-model="loginForm.password" type="password" placeholder="请输入密码" />
      </view>
      <view class="form-item row">
        <view class="checkbox-label" @tap="autoLogin = !autoLogin">
          <view class="check-box" :class="{ 'check-box-active': autoLogin }">
            <text v-if="autoLogin" class="check-mark">✓</text>
          </view>
          <text>自动登录</text>
        </view>
      </view>
      <button class="btn-primary" @tap="handleLogin">登 录</button>
      <view class="switch-link">
        <text>还没有账号？</text>
        <text class="link" @tap="isRegister = true; clearForm()">立即注册</text>
      </view>
    </view>

    <!-- 注册表单 -->
    <view class="form-card" v-else>
      <view class="form-title">注册</view>
      <view class="form-item">
        <text class="label">用户名</text>
        <input class="input" v-model="regForm.username" placeholder="请输入用户名" />
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <input class="input" v-model="regForm.phone" placeholder="请输入手机号" maxlength="11" type="number" />
      </view>
      <view class="form-item">
        <text class="label">密码</text>
        <input class="input" v-model="regForm.password" type="password" placeholder="密码至少6位" />
      </view>
      <view class="form-item">
        <text class="label">确认密码</text>
        <input class="input" v-model="regForm.confirmPwd" type="password" placeholder="再次输入密码" />
      </view>
      <button class="btn-primary" @tap="handleRegister">注 册</button>
      <view class="switch-link">
        <text>已有账号？</text>
        <text class="link" @tap="isRegister = false; clearForm()">返回登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { apiLogin, apiRegister } from "../../utils/api";
import { setCurrentUserId, clearCurrentUser } from "../../utils/storage";

export default {
  data() {
    return {
      isRegister: false,
      autoLogin: true,
      loginForm: { account: "", password: "" },
      regForm: { username: "", phone: "", password: "", confirmPwd: "" }
    };
  },
  onLoad() {
    this.checkAutoLogin();
  },
  onShow() {
    const uid = uni.getStorageSync("current_user_id");
    if (uid) uni.switchTab({ url: "/pages/auction/auction" });
  },
  methods: {
    checkAutoLogin() {
      const uid = uni.getStorageSync("current_user_id");
      if (uid) uni.switchTab({ url: "/pages/auction/auction" });
    },
    clearForm() {
      this.loginForm = { account: "", password: "" };
      this.regForm = { username: "", phone: "", password: "", confirmPwd: "" };
    },
    async handleLogin() {
      const { account, password } = this.loginForm;
      if (!account.trim()) return uni.showToast({ title: "请输入账号", icon: "none" });
      if (!password) return uni.showToast({ title: "请输入密码", icon: "none" });
      try {
        const data = await apiLogin(account.trim(), password);
        if (!this.autoLogin) clearCurrentUser();
        uni.showToast({ title: "登录成功", icon: "success" });
        setTimeout(() => uni.switchTab({ url: "/pages/auction/auction" }), 500);
      } catch (e) {
        uni.showToast({ title: e.message, icon: "none" });
      }
    },
    async handleRegister() {
      const { username, phone, password, confirmPwd } = this.regForm;
      if (!username.trim()) return uni.showToast({ title: "请输入用户名", icon: "none" });
      if (!phone.trim()) return uni.showToast({ title: "请输入手机号", icon: "none" });
      if (!/^1\d{10}$/.test(phone.trim())) return uni.showToast({ title: "手机号格式不正确", icon: "none" });
      if (password.length < 6) return uni.showToast({ title: "密码至少6位", icon: "none" });
      if (password !== confirmPwd) return uni.showToast({ title: "两次密码不一致", icon: "none" });
      try {
        await apiRegister(username.trim(), password, username.trim(), phone.trim());
        uni.showToast({ title: "注册成功", icon: "success" });
        this.isRegister = false;
        this.loginForm.account = username.trim();
        this.loginForm.password = "";
        this.regForm = { username: "", phone: "", password: "", confirmPwd: "" };
      } catch (e) {
        uni.showToast({ title: e.message, icon: "none" });
      }
    }
  }
};
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40rpx;
  box-sizing: border-box;
}
.form-card {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}
.form-title {
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 48rpx;
}
.form-item { margin-bottom: 32rpx; }
.form-item .label {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 12rpx;
}
.form-item .input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  background: #fafafa;
}
.form-item.row { display: flex; align-items: center; }
.checkbox-label {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #888;
}
.check-box {
  width: 36rpx; height: 36rpx;
  border: 2rpx solid #ccc;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10rpx;
  flex-shrink: 0;
}
.check-box-active { background: #007AFF; border-color: #007AFF; }
.check-mark { color: #fff; font-size: 24rpx; font-weight: bold; }
.btn-primary {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #007AFF, #0056D6);
  color: #fff;
  font-size: 32rpx;
  border-radius: 16rpx;
  margin-top: 20rpx;
  border: none;
}
.btn-primary::after { border: none; }
.switch-link {
  text-align: center;
  margin-top: 36rpx;
  font-size: 26rpx;
  color: #999;
}
.switch-link .link { color: #007AFF; margin-left: 8rpx; }
</style>
