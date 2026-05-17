<template>
   <el-timeline>
      <el-timeline-item
         v-for="log in logs"
         :key="log.logId"
         :timestamp="parseTime(log.operateTime)"
         placement="top"
      >
         <el-card shadow="never">
            <p style="margin: 0">
               <el-tag size="small">{{ log.actionName }}</el-tag>
               <span style="margin-left: 8px">{{ log.operatorName }}</span>
               <span v-if="log.fromStatus || log.toStatus" style="margin-left: 8px; color: #909399">
                  {{ formatStatus(log.fromStatus) }} → {{ formatStatus(log.toStatus) }}
               </span>
            </p>
            <p v-if="log.comment" style="margin: 5px 0 0 0; color: #606266">{{ log.comment }}</p>
         </el-card>
      </el-timeline-item>
   </el-timeline>
   <el-empty v-if="logs.length === 0" description="暂无操作日志" />
</template>

<script setup>
defineProps({
   logs: {
      type: Array,
      default: () => []
   }
})

const { itsm_ticket_status } = useDict("itsm_ticket_status")

function formatStatus(code) {
   if (!code) return ""
   const dict = itsm_ticket_status.value.find(d => d.value === code)
   return dict ? dict.label : code
}
</script>
