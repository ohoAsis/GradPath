<template>
  <div style="padding: 20px;">
    <h2>Reviewer · Review Detail</h2>
    
    <div v-if="error" style="color: red; margin: 16px 0;">
      Failed to load review summary
    </div>
    
    <div v-else-if="loading" style="margin: 16px 0;">
      Loading review summary...
    </div>
    
    <div v-else-if="reviewData">
      <!-- Application 级别信息 -->
      <div style="border: 1px solid #ddd; border-radius: 4px; padding: 16px; margin: 16px 0;">
        <h3>Application Information</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px;">
          <div>
            <strong>Application ID:</strong> {{ reviewData.applicationId }}
          </div>
          <div>
            <strong>Application Status:</strong> {{ reviewData.applicationStatus }}
          </div>
        </div>
      </div>
      
      <!-- Material 级别信息 -->
      <div style="border: 1px solid #ddd; border-radius: 4px; padding: 16px; margin: 16px 0;">
        <h3>Materials</h3>
        <table style="width: 100%; border-collapse: collapse; margin-top: 12px;">
          <thead>
            <tr style="border-bottom: 2px solid #ddd;">
              <th style="padding: 12px; text-align: left;">Material ID</th>
              <th style="padding: 12px; text-align: left;">Current Version</th>
              <th style="padding: 12px; text-align: left;">Aggregation Result</th>
              <th style="padding: 12px; text-align: left;">Blocking Reason</th>
              <th style="padding: 12px; text-align: left;">Effective Reviewer Count</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="material in reviewData.materials" 
              :key="material.materialId"
              style="border-bottom: 1px solid #ddd;"
            >
              <td style="padding: 12px;">{{ material.materialId }}</td>
              <td style="padding: 12px;">{{ material.currentVersion }}</td>
              <td style="padding: 12px;">{{ material.aggregationResult }}</td>
              <td style="padding: 12px;">{{ material.blockingReason || '-' }}</td>
              <td style="padding: 12px;">{{ material.effectiveReviewerCount }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const reviewData = ref(null);
const loading = ref(true);
const error = ref(null);

const fetchReviewSummary = async () => {
  try {
    const id = route.params.id;
    const response = await fetch(`/applications/${id}/review-summary`);
    const data = await response.json();
    reviewData.value = data;
  } catch (err) {
    error.value = err;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchReviewSummary();
});
</script>
