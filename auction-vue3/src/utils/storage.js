/**
 * utils/storage.js — 本地存储工具
 * 部分跨页面共享数据仍使用 storage
 */

export function getCurrentUserId() {
  return uni.getStorageSync("current_user_id") || null;
}

export function setCurrentUserId(id) {
  uni.setStorageSync("current_user_id", id);
}

export function getCurrentNickname() {
  return uni.getStorageSync("current_user_nickname") || "";
}

export function clearCurrentUser() {
  uni.removeStorageSync("current_user_id");
  uni.removeStorageSync("current_user_nickname");
  uni.removeStorageSync("current_user_token");
}
