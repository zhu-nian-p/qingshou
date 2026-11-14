<template>
  <div class="publish-page">
    <header class="topbar">
      <button class="back" @click="goBack">‹</button>
      <h1>发布闲置</h1>
      <div class="logo">青售</div>
    </header>

    <div class="wrap">
      <div class="form-card">
        <div class="field">
          <label>商品图片</label>
          <el-upload
            :show-file-list="false"
            accept="image/*"
            :before-upload="beforeUpload"
            :http-request="uploadImage">
            <div class="upload-box">
              <template v-if="form.imageUrl">
                <img :src="imgSrc(form.imageUrl)" alt="商品图" @error="onImgError">
                <div class="upload-mask">点击更换图片</div>
              </template>
              <template v-else>
                <div class="plus">＋</div>
                <div class="u-tip">点击上传商品图片</div>
              </template>
            </div>
          </el-upload>
          <div class="tip">支持 jpg / png / webp，单张不超过 5MB</div>
          <el-button v-if="form.imageUrl" link type="danger" size="small" @click="form.imageUrl = ''">移除图片</el-button>
        </div>

        <div class="field">
          <label>商品标题<span class="req">*</span></label>
          <el-input v-model="form.title" size="large" maxlength="100" show-word-limit placeholder="例如：九成新自行车，毕业急出"></el-input>
        </div>

        <div class="field">
          <label>商品描述</label>
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="成色、入手时间、使用情况、瑕疵等，写清楚更好卖～"></el-input>
        </div>

        <div class="grid-2">
          <div class="field">
            <label>价格（元）<span class="req">*</span></label>
            <el-input-number v-model="form.price" :min="0.01" :max="99999999" :precision="2" controls-position="right" style="width:100%"></el-input-number>
          </div>
          <div class="field">
            <label>分类<span class="req">*</span></label>
            <el-select v-model="form.category" placeholder="请选择分类" size="large" style="width:100%">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c"></el-option>
            </el-select>
          </div>
        </div>

        <div class="grid-2">
          <div class="field">
            <label>校区<span class="req">*</span></label>
            <el-select v-model="form.campus" placeholder="请选择校区" size="large" style="width:100%">
              <el-option v-for="c in campuses" :key="c" :label="c" :value="c"></el-option>
            </el-select>
          </div>
          <div class="field">
            <label>联系方式（微信 / QQ）<span class="req">*</span></label>
            <el-input v-model="form.contactInfo" size="large" maxlength="100" placeholder="例如：wx: xiaoming01"></el-input>
          </div>
        </div>
      </div>
    </div>

    <div class="submit-bar">
      <button class="submit-btn" :disabled="submitting" @click="submit">
        {{ submitting ? '发布中…' : '发 布 闲 置' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api } from '@/api/http';
import { imgSrc, onImgError } from '@/utils/media';

const router = useRouter();
const categories = ['数码', '书籍', '生活用品', '运动户外', '其他'];
const campuses = ['上海路', '前湖'];
const submitting = ref(false);
const form = reactive({
  title: '', description: '', price: null,
  category: '', campus: '前湖', contactInfo: '', imageUrl: ''
});

function goBack() {
  if (history.length > 1) history.back();
  else router.push('/');
}

function beforeUpload(file) {
  if (!/^image\//.test(file.type)) {
    ElMessage.error('只能上传图片文件');
    return false;
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片不能超过 5MB');
    return false;
  }
  return true;
}

async function uploadImage({ file }) {
  try {
    const data = await api.upload(file);
    form.imageUrl = data.url;
    ElMessage.success('图片上传成功');
  } catch (e) {
    ElMessage.error(e.message || '图片上传失败');
  }
}

async function submit() {
  if (!form.title.trim()) return ElMessage.warning('请填写商品标题');
  if (!form.price || Number(form.price) <= 0) return ElMessage.warning('价格必须大于 0');
  if (!form.category) return ElMessage.warning('请选择分类');
  if (!form.campus) return ElMessage.warning('请选择校区');
  if (!form.contactInfo.trim()) return ElMessage.warning('请填写联系方式');

  submitting.value = true;
  try {
    await api.post('/api/item', {
      title: form.title.trim(),
      description: form.description.trim(),
      price: Number(form.price),
      category: form.category,
      campus: form.campus,
      contactInfo: form.contactInfo.trim(),
      imageUrl: form.imageUrl || null
    });
    await ElMessageBox.alert('发布成功', '青售提示', { confirmButtonText: '返回首页', type: 'success' });
    router.push('/');
  } catch (e) {
    ElMessage.error(e.message || '发布失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.publish-page { padding-bottom: 90px; }
.topbar {
  position: sticky; top: 0; z-index: 10; display: flex; align-items: center; gap: 10px;
  padding: 12px 16px; background: rgba(255,255,255,.92); backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line);
}
.topbar .back { border: none; background: #e6fffa; color: var(--teal-dark); width: 34px; height: 34px; border-radius: 10px; font-size: 16px; cursor: pointer; }
.topbar h1 { font-size: 17px; margin: 0; letter-spacing: 1px; }
.topbar .logo { margin-left: auto; font-size: 20px; }
.wrap { max-width: 720px; margin: 0 auto; padding: 16px 14px; }
.form-card { background: #fff; border: 1px solid var(--line); border-radius: 18px; padding: 18px 16px; box-shadow: 0 10px 26px rgba(13,148,136,.06); }
.field { margin-bottom: 18px; }
.field > label { display: block; font-size: 13px; font-weight: 700; color: #36635c; margin-bottom: 8px; }
.field .req { color: #ef4444; margin-left: 2px; }
.tip { font-size: 11px; color: #9bb5b0; margin-top: 6px; }
.upload-box {
  width: 100%; aspect-ratio: 16 / 9; max-height: 260px; border: 2px dashed #c8eee7;
  border-radius: 14px; background: #f4fffd; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 6px; color: #7fb3ab;
  cursor: pointer; overflow: hidden; position: relative;
}
.upload-box .plus { font-size: 30px; color: var(--teal); }
.upload-box .u-tip { font-size: 12px; }
.upload-box img { width: 100%; height: 100%; object-fit: cover; }
.upload-mask { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(15,46,42,.36); color: #fff; font-size: 13px; opacity: 0; transition: opacity .15s; }
.upload-box:hover .upload-mask { opacity: 1; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (max-width: 520px) { .grid-2 { grid-template-columns: 1fr; } }
.submit-bar {
  position: fixed; left: 0; right: 0; bottom: 0; z-index: 20;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255,255,255,.96); border-top: 1px solid var(--line);
  backdrop-filter: blur(10px);
}
.submit-btn {
  display: block; width: 100%; max-width: 720px; margin: 0 auto; height: 50px;
  border: none; border-radius: 16px; cursor: pointer;
  background: linear-gradient(135deg, #14b8a6, #0d9488); color: #fff;
  font-size: 16px; font-weight: 700; letter-spacing: 2px;
  box-shadow: 0 10px 24px rgba(13,148,136,.28);
}
.submit-btn[disabled] { opacity: .65; cursor: not-allowed; }
</style>
