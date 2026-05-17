<template>
   <el-dialog title="状态流转" :model-value="visible" @update:model-value="$emit('update:visible', $event)" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
         <el-form-item label="目标状态" prop="targetStatus">
            <el-select v-model="form.targetStatus" placeholder="请选择目标状态">
               <el-option
                  v-for="t in transitions"
                  :key="t.code"
                  :label="t.label"
                  :value="t.code"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" placeholder="请输入流转备注" />
         </el-form-item>
      </el-form>
      <template #footer>
         <div class="dialog-footer">
            <el-button type="primary" @click="handleSubmit">确 定</el-button>
            <el-button @click="$emit('update:visible', false)">取 消</el-button>
         </div>
      </template>
   </el-dialog>
</template>

<script setup>
const props = defineProps({
   visible: {
      type: Boolean,
      default: false
   },
   transitions: {
      type: Array,
      default: () => []
   },
   modelValue: {
      type: Object,
      default: () => ({})
   }
})

const emit = defineEmits(['update:visible', 'submit'])

const { proxy } = getCurrentInstance()
const form = ref({ ...props.modelValue })
const rules = {
   targetStatus: [{ required: true, message: "目标状态不能为空", trigger: "change" }]
}

watch(() => props.modelValue, (val) => {
   form.value = { ...val }
}, { deep: true })

function handleSubmit() {
   proxy.$refs["formRef"].validate(valid => {
      if (valid) {
         emit('submit', form.value)
      }
   })
}
</script>
