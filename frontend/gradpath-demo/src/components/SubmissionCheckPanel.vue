<template>
  <div style="border: 1px solid #ddd; border-radius: 4px; padding: 16px; margin: 16px 0;">
    <h3>Submission Check</h3>
    
    <div style="margin-bottom: 16px; font-weight: bold;">
      Can Submit: {{ canSubmit ? 'Yes' : 'No' }}
    </div>
    
    <div>
      <h4>Checks</h4>
      <div v-for="(check, index) in checks" :key="index" style="margin: 8px 0; padding: 8px; background-color: #f9f9f9;" :style="{ 'border-left': `4px solid ${check.passed ? '#4CAF50' : '#f44336'}` }">
        <div style="display: flex; align-items: center; gap: 8px;">
          <span>{{ check.type }}</span>
          <span>{{ check.passed ? '✅' : '❌' }}</span>
        </div>
        <div v-if="!check.passed && check.failureReason" style="margin-top: 4px; color: #666; font-size: 14px;">
          Reason: {{ check.failureReason }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

defineProps({
  canSubmit: {
    type: Boolean,
    required: true
  },
  checks: {
    type: Array,
    required: true
  }
});
</script>