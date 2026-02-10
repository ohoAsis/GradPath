<template>
  <div style="border: 1px solid #ddd; border-radius: 4px; padding: 16px; margin: 16px 0;">
    <h3>Lifecycle Actions</h3>
    
    <div style="margin-bottom: 16px;">
      <h4>Allowed Actions</h4>
      <div style="display: flex; gap: 8px; flex-wrap: wrap;">
        <button 
          v-for="action in allowedActions" 
          :key="action"
          style="padding: 6px 12px; background-color: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer;"
          @click="console.log('Action clicked:', action)"
        >
          {{ action }}
        </button>
      </div>
    </div>
    
    <div>
      <h4>Blocked Actions</h4>
      <div v-for="item in blockedActions" :key="item.action" style="margin: 8px 0;">
        <div style="display: flex; align-items: center; gap: 8px;">
          <button 
            style="padding: 6px 12px; background-color: #ccc; color: #666; border: none; border-radius: 4px; cursor: not-allowed;"
            disabled
          >
            {{ item.action }}
          </button>
          <span style="color: #666; font-size: 14px;">{{ item.reason }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

defineProps({
  allowedActions: {
    type: Array,
    required: true
  },
  blockedActions: {
    type: Array,
    required: true
  }
});
</script>