<script setup>
import { computed } from 'vue'

const props = defineProps({
  appType: {
    type: String,
    required: true
  },
  activeChatId: {
    type: String,
    default: ''
  },
  conversations: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'delete', 'new-chat'])

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const now = Date.now()
  const then = new Date(dateStr).getTime()
  const diff = now - then
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} 天前`
  const months = Math.floor(days / 30)
  return `${months} 月前`
}

const isEmpty = computed(() => !props.loading && props.conversations.length === 0)
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <button class="sidebar-new-btn" @click="emit('new-chat')">
        + 新建对话
      </button>
    </div>

    <div class="sidebar-list">
      <p v-if="isEmpty" class="sidebar-empty">暂无对话记录</p>

      <div
        v-for="conv in conversations"
        :key="conv.chatKey"
        class="sidebar-item"
        :class="{ 'sidebar-item-active': conv.chatKey === activeChatId }"
        @click="emit('select', conv)"
      >
        <div class="sidebar-item-content">
          <span class="sidebar-item-title">{{ conv.title || '未命名对话' }}</span>
          <span class="sidebar-item-time">{{ timeAgo(conv.updatedAt) }}</span>
        </div>
        <button
          class="sidebar-item-delete"
          title="删除对话"
          @click.stop="emit('delete', conv)"
        >
          ×
        </button>
      </div>
    </div>
  </aside>
</template>
