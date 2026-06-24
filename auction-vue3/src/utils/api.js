/**
 * utils/api.js ?后端 API 调用封装
 * 所有用户端接口统一通过此模块调? */

const API_BASE = "http://127.0.0.1:8080/api/user-api";
const UPLOAD_BASE = "http://127.0.0.1:8080/api/upload";
const STATIC_BASE = "http://127.0.0.1:8080";

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
        try {
          if (res && res.data && res.data.code === 200) {
            resolve(res.data.data);
          } else {
            const msg = (res && res.data && res.data.message) || "请求失败";
            reject(new Error(String(msg)));
          }
        } catch (e) {
          reject(new Error("请求失败: " + String((e && e.message) || "未知错误")));
        }
      },
      fail: (err) => {
        reject(new Error("网络请求失败,请检查后端服务"));
      }
    });
  });
}

// ==================== 用户相关 ====================
export function apiRegister(username, password, nickname, phone) {
  return request("POST", "/register", { username, password, nickname, phone }, false);
}

export function apiLogin(account, password) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: API_BASE + "/login",
      method: "POST",
      data: { account, password },
      header: { "Content-Type": "application/json" },
      success: (res) => {
        try {
          if (res && res.data && res.data.code === 200) {
            const d = res.data.data || {};
            uni.setStorageSync("current_user_id", d.userId || "");
            uni.setStorageSync("current_user_nickname", d.nickname || "");
            uni.setStorageSync("current_user_token", d.token || "");
            resolve(d);
          } else {
            const msg = (res && res.data && res.data.message) || "登录失败";
            reject(new Error(String(msg)));
          }
        } catch (e) {
          reject(new Error("登录失败: " + String((e && e.message) || "未知错误")));
        }
      },
      fail: (err) => {
        reject(new Error("网络请求失败,请检查后端服务"));
      }
    });
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

export function apiRelist(id) {
  return request("PUT", "/goods/" + id + "/relist", {});
}

export function apiDeleteGoods(id) {
  return request("DELETE", "/goods/" + id);
}

export function apiMyGoodsAll(status, keyword) {
  let url = "/goods/my-goods-all";
  const params = [];
  if (status) params.push("status=" + encodeURIComponent(status));
  if (keyword) params.push("keyword=" + encodeURIComponent(keyword));
  if (params.length) url += "?" + params.join("&");
  return request("GET", url);
}

export function apiCancelBid(bidId) {
  return request("POST", "/bid/cancel", { bidId });
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

// ==================== 拍卖结束 ====================
export function apiGoodsBids(goodsId) {
  return request("GET", "/goods/" + goodsId + "/bids");
}

export function apiCloseAuction(goodsId) {
  return request("POST", "/goods/" + goodsId + "/close", {});
}

export function apiCheckExpired() {
  return request("POST", "/goods/check-expired", {});
}

// ==================== 账单流水 ====================
export function apiBills(type, limit) {
  let url = "/bill/list";
  const params = [];
  if (type) params.push("type=" + encodeURIComponent(type));
  if (limit) params.push("limit=" + limit);
  if (params.length) url += "?" + params.join("&");
  return request("GET", url);
}

// ==================== 文件上传 ====================
export function apiUploadImage(filePath) {
  return new Promise((resolve, reject) => {
    const userId = uni.getStorageSync("current_user_id");
    const header = {};
    if (userId) header["X-User-Id"] = userId;
    uni.uploadFile({
      url: UPLOAD_BASE + "/image",
      filePath,
      name: "file",
      header,
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          if (data && data.code === 200) {
            // 后端返回绝对路径 /uploads/...  -> 加 host
            const url = data.data.url;
            resolve({ ...data.data, fullUrl: url.startsWith("http") ? url : STATIC_BASE + url });
          } else {
            reject(new Error((data && data.message) || "上传失败"));
          }
        } catch (e) {
          reject(new Error("上传失败: " + (e && e.message || "未知")));
        }
      },
      fail: (err) => reject(new Error("网络请求失败,请检查后端服务"))
    });
  });
}

// ==================== 售后 ====================
export function apiApplyRefund(orderId, reason) {
  return request("POST", "/order/" + orderId + "/apply-refund", { reason: reason || "" });
}

export function apiOrderDetail(id) {
  return request("GET", "/order/" + id, null, false);
}
