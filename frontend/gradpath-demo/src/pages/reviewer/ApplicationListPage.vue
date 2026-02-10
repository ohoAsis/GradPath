<template>
  <div style="padding: 20px;">
    <h2>Reviewer · Application List</h2>
    
    <div v-if="error" style="color: red; margin: 16px 0;">
      Failed to load applications
    </div>
    
    <div v-else-if="loading" style="margin: 16px 0;">
      Loading applications...
    </div>
    
    <div v-else>
      <table style="width: 100%; border-collapse: collapse; margin: 16px 0;">
        <thead>
          <tr style="border-bottom: 2px solid #ddd;">
            <th style="padding: 12px; text-align: left;">Application ID</th>
            <th style="padding: 12px; text-align: left;">Status</th>
            <th style="padding: 12px; text-align: left;">Stage</th>
            <th style="padding: 12px; text-align: left;">Overall Conclusion</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="app in applications" 
            :key="app.applicationId"
            style="border-bottom: 1px solid #ddd; cursor: pointer;"
            @click="navigateToApplication(app.applicationId)"
          >
            <td style="padding: 12px;">{{ app.applicationId }}</td>
            <td style="padding: 12px;">{{ app.applicationStatus }}</td>
            <td style="padding: 12px;">{{ app.stage }}</td>
            <td style="padding: 12px;">{{ app.overallConclusion || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const applications = ref([]);
const loading = ref(true);
const error = ref(null);

const fetchApplications = async () => {
  try {
    const response = await fetch('/applications/overview');
    const data = await response.json();
    applications.value = data;
  } catch (err) {
    error.value = err;
  } finally {
    loading.value = false;
  }
};

const navigateToApplication = (id) => {
  router.push(`/reviewer/application/${id}`);
};

onMounted(() => {
  fetchApplications();
});
</script>