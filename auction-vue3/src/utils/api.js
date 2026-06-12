/**
 * utils/api.js — 后端 API 调用封装
 * 所有用户端接口统一通过此模块调用
 */

const API_BASE = "http://localhost:8080/api/user-api";

/**
 * 通用请求方法
 */
function request(method, url, data, useAuth = true) {
  return new Promise((resolve, reject) => {
    const header = { "Content-Type": "application/json" };
    if (useAuth) {
      const userId = uni.getStorageSync("current_user_id");
      if (userId) header["X-User-Id"] = userId;
    }
    uni.request({
      url: API_BASE + url,
      method,
      data,
      header,
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data.data);
        } else {
          reject(new Error(res.data.message || "请求失败"));
        }
      },
      fail: (err) => {
        reject(new Error("网络请求失败: " + JSON.stringify(err)));
      }
    });
  });
}

// ==================== 用户相关 ====================
export function apiRegister(username, password, nickname, phone) {
  return request("POST", "/register", { username, password, nickname, phone }, false);
}

export function apiLogin(account, password) {
  return uni.request({
    url: API_BASE + "/login",
    method: "POST",
    data: { account, password },
    header: { "Content-Type": "application/json" },
  }).then(res => {
    if (res.data.code === 200) {
      const d = res.data.data;
      uni.setStorageSync("current_user_id", d.userId);
      uni.setStorageSync("current_user_nickname", d.nickname);
      uni.setStorageSync("current_user_token", d.token);
      return d;
    }
    throw new Error(res.data.message || "登录失败");
  });
}

export function apiGetProfile() {
  return request("GET", "/profile");
}

export function apiRecharge(amount) {
  return request("PUT", "/balance/recharge", { amount });
}

// ==================== 商品相关 ====================
export function apiGoodsList(keyword, type) {
  let url = "/goods/list";
  const params = [];
  if (keyword) params.push("keyword=" + encodeURIComponent(keyword));
  if (type) params.push("type=" + encodeURIComponent(type));
  if (params.length) url += "?" + params.join("&");
  return request("GET", url, null, false);
}

export function apiGoodsDetail(id) {
  return request("GET", "/goods/" + id, null, false);
}

export function apiAddGoods(goods) {
  return request("POST", "/goods/add", goods);
}

export function apiMyGoods() {
  return request("GET", "/goods/my-goods");
}

export function apiOffShelf(id) {
  return request("PUT", "/goods/" + id + "/off-shelf", {});
}

// ==================== 竞拍相关 ====================
export function apiPlaceBid(goodsId, price) {
  return request("POST", "/bid/place", { goodsId, price });
}

export function apiMyBids() {
  return request("GET", "/bid/my-bids");
}

// ==================== 订单相关 ====================
export function apiPurchases() {
  return request("GET", "/order/purchases");
}

export function apiSales() {
  return request("GET", "/order/sales");
}

export function apiOrderDetail(id) {
  return request("GET", "/order/" + id, null, false);
}
