<template>
  <div style="padding: 20px;">
    <h2>Student · My Application</h2>
    
    <div v-if="error" style="color: red; margin: 16px 0;">
      Failed to load application data
    </div>
    
    <div v-else-if="!lifecycleData || !submissionCheckData" style="margin: 16px 0;">
      Loading...
    </div>
    
    <template v-else>
      <!-- 基础信息区 -->
      <div style="border: 1px solid #ddd; border-radius: 4px; padding: 16px; margin: 16px 0;">
        <h3>Basic Information</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px;">
          <div>
            <strong>Application ID:</strong> {{ lifecycleData.id }}
          </div>
          <div>
            <strong>Application Status:</strong> {{ lifecycleData.status }}
          </div>
          <div>
            <strong>Stage:</strong> {{ lifecycleData.stage }}
          </div>
          <div v-if="lifecycleData.overallConclusion">
            <strong>Overall Conclusion:</strong> {{ lifecycleData.overallConclusion }}
          </div>
        </div>
      </div>
      
      <!-- LifecyclePanel -->
      <LifecyclePanel 
        :allowedActions="lifecycleData.allowedActions"
        :blockedActions="lifecycleData.blockedActions"
      />
      
      <!-- SubmissionCheckPanel -->
      <SubmissionCheckPanel 
        :canSubmit="submissionCheckData.canSubmit"
        :checks="submissionCheckData.checks"
      />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import LifecyclePanel from '../../components/LifecyclePanel.vue';
import SubmissionCheckPanel from '../../components/SubmissionCheckPanel.vue';

const lifecycleData = ref(null);
const submissionCheckData = ref(null);
const error = ref(null);

const fetchApplicationData = async () => {
  try {
    const [lifecycleResponse, submissionCheckResponse] = await Promise.all([
      fetch('/applications/1/lifecycle-summary'),
      fetch('/applications/1/submission-check')
    ]);
    
    const lifecycleJson = await lifecycleResponse.json();
    const submissionCheckJson = await submissionCheckResponse.json();
    
    lifecycleData.value = lifecycleJson;
    submissionCheckData.value = submissionCheckJson;
  } catch (err) {
    error.value = err;
  }
};

onMounted(() => {
  fetchApplicationData();
});
</script>