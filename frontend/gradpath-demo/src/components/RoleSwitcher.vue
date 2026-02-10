<template>
  <div style="padding: 8px; border-bottom: 1px solid #ddd; display: flex; justify-content: space-between; align-items: center;">
    <strong>Role Switcher (Demo)</strong>
    <div>
      <button 
        :style="{ marginRight: '8px', padding: '4px 12px', backgroundColor: currentRole === 'Student' ? '#e3f2fd' : 'white', border: '1px solid #ddd' }"
        @click="switchRole('Student')"
      >
        Student
      </button>
      <button 
        :style="{ marginRight: '8px', padding: '4px 12px', backgroundColor: currentRole === 'Reviewer' ? '#e8f5e8' : 'white', border: '1px solid #ddd' }"
        @click="switchRole('Reviewer')"
      >
        Reviewer
      </button>
      <button 
        :style="{ padding: '4px 12px', backgroundColor: currentRole === 'Admin' ? '#fff3e0' : 'white', border: '1px solid #ddd' }"
        @click="switchRole('Admin')"
      >
        Admin
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const currentRole = ref('Student');

const switchRole = (role) => {
  currentRole.value = role;
  
  switch (role) {
    case 'Student':
      router.push('/student/application/1');
      break;
    case 'Reviewer':
      router.push('/reviewer/applications');
      break;
    case 'Admin':
      router.push('/admin/overview');
      break;
  }
};
</script>
