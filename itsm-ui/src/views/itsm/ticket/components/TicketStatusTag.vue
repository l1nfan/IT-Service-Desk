<template>
   <dict-tag :options="itsm_ticket_status" :value="status" />
</template>

<script setup>
defineProps({
   status: {
      type: String,
      default: ''
   }
})

const { itsm_ticket_status } = useDict("itsm_ticket_status")
</script>
