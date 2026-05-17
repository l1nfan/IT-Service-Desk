<template>
   <div class="app-container">
      <el-empty description="工单仪表盘开发中，敬请期待">
         <template #image>
            <el-icon :size="120" color="#909399"><DataAnalysis /></el-icon>
         </template>
      </el-empty>
   </div>
</template>

<script setup name="ItsmDashboard">
import { DataAnalysis } from '@element-plus/icons-vue'
</script>
