<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑商品"
    width="480px"
    align-center
    @update:model-value="$emit('update:modelValue', $event)">
    <div class="dialog-field">
      <label>标题</label>
      <el-input v-model="form.title" maxlength="100" show-word-limit></el-input>
    </div>
    <div class="dialog-field">
      <label>描述</label>
      <el-input v-model="form.description" type="textarea" :rows="3" maxlength="2000" show-word-limit></el-input>
    </div>
    <div class="dialog-grid">
      <div class="dialog-field">
        <label>价格（元）</label>
        <el-input-number v-model="form.price" :min="0.01" :precision="2" controls-position="right" style="width:100%"></el-input-number>
      </div>
      <div class="dialog-field">
        <label>分类</label>
        <el-select v-model="form.category" style="width:100%">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c"></el-option>
        </el-select>
      </div>
    </div>
    <div class="dialog-grid">
      <div class="dialog-field">
        <label>校区</label>
        <el-select v-model="form.campus" style="width:100%">
          <el-option v-for="c in campuses" :key="c" :label="c" :value="c"></el-option>
        </el-select>
      </div>
      <div class="dialog-field">
        <label>联系方式</label>
        <el-input v-model="form.contactInfo" maxlength="100" placeholder="微信 / QQ"></el-input>
      </div>
    </div>
    <div class="dialog-field">
      <label>商品图片</label>
      <el-upload
        :action="uploadUrl"
        :headers="uploadHeaders"
        name="file"
        accept="image/*"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :on-success="onUploadSuccess"
        :on-error="onUploadError">
        <div class="edit-upload-box">
          <img v-if="form.imageUrl" :src="imgSrc(form.imageUrl)" alt="商品图" @error="onImgError">
          <span v-else>＋ 上传图片</span>
        </div>
      </el-upload>
    </div>
    <template #footer>
      <el-button round @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" round :loading="saving" @click="save">保存修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { api, API_BASE } from '@/api/http';
import { imgSrc, onImgError } from '@/utils/media';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  item: { type: Object, default: null }
});
const emit = defineEmits(['update:modelValue', 'saved']);

const categories = ['数码', '书籍', '生活用品', '运动户外', '其他'];
const campuses = ['上海路', '前湖'];
const uploadUrl = API_BASE + '/api/upload';
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + (localStorage.getItem('qingshou_token') || '')
}));

const saving = ref(false);
const form = reactive({
  title: '', description: '', price: null,
  category: '', campus: '前湖', contactInfo: '', imageUrl: ''
});

watch(
  () => [props.modelValue, props.item],
  async ([visible, item]) => {
    if (!visible || !item) return;
    form.title = item.title || '';
    form.description = '';
    form.price = Number(item.price);
    form.category = item.category || '其他';
    form.campus = item.campus || '前湖';
    form.contactInfo = '';
    form.imageUrl = item.imageUrl || '';
    try {
      const d = await api.get('/api/item/' + item.id);
      form.title = d.title || form.title;
      form.description = d.description || '';
      form.contactInfo = d.contactInfo || '';
      form.price = Number(d.price);
      form.category = d.category || form.category;
      form.campus = d.campus || form.campus;
      form.imageUrl = d.imageUrl || '';
    } catch (e) {
      // 详情拉取失败时保留列表数据
    }
  }
);

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

function onUploadSuccess(res) {
  if (res && res.code === 200 && res.data && res.data.url) {
    form.imageUrl = res.data.url;
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error((res && res.message) || '图片上传失败');
  }
}

function onUploadError() {
  ElMessage.error('图片上传失败，请检查登录状态或网络');
}

async function save() {
  if (!props.item) return;
  if (!form.title || !form.title.trim()) return ElMessage.warning('请填写商品标题');
  if (!form.price || Number(form.price) <= 0) return ElMessage.warning('价格必须大于 0');
  if (!form.category) return ElMessage.warning('请选择分类');
  saving.value = true;
  try {
    await api.put('/api/item/' + props.item.id, {
      title: form.title.trim(),
      description: form.description.trim(),
      price: Number(form.price),
      category: form.category,
      campus: form.campus,
      contactInfo: form.contactInfo.trim(),
      imageUrl: form.imageUrl || null
    });
    ElMessage.success('修改成功');
    emit('update:modelValue', false);
    emit('saved');
  } catch (e) {
    ElMessage.error(e.message || '保存失败');
  } finally {
    saving.value = false;
  }
}
</script>
