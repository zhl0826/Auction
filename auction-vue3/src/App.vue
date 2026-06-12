<script>
import { getCurrentUserId } from './utils/storage';
export default {
  globalData: {
    userInfo: null
  },
  onLaunch() {
    console.log('App Launch');
    const userId = getCurrentUserId();
    if (userId) {
      console.log('[App] auto-login user id:', userId);
    }
  },
  onShow() {
    console.log('App Show');
  },
  onHide() {
    console.log('App Hide');
  }
};
</script>

<style>
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>