<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { Twitter, MessageCircle, Send } from 'lucide-vue-next'

const { t } = useI18n()
</script>

<template>
  <footer style="border-top: 1px solid #e5e7eb; background: #f9fafb;">
    <div style="max-width: 1536px; margin: 0 auto; padding: 24px 16px;">
      <div style="text-align: center; font-size: 12px; color: #9ca3af;">
        © {{ new Date().getFullYear() }} Pet AI. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<style scoped>
.gradient-text {
  background: linear-gradient(135deg, #7c3aed, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-link {
  font-size: 14px;
  color: #6b7280;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-link:hover {
  color: #2563eb;
}

.social-link {
  color: #6b7280;
  transition: color 0.2s;
  display: inline-flex;
}

.social-link:hover {
  color: #2563eb;
}
</style>
