<script setup lang="ts">
defineProps<{
  columns: Array<{ key: string, label: string, sticky?: boolean }>
  data: any[]
  actions?: Function
}>();
</script>

<template>
  <div class="table-wrapper">
    <table>
      <thead>
      <tr>
        <th v-for="col in columns"
            :key="col.key"
            :class="{ 'sticky-col': col.sticky }"
        >
          {{ col.label }}
        </th>
      </tr>
      </thead>

      <tbody>
      <tr v-for="(row, i) in data" :key="i">
        <td v-for="col in columns"
            :key="col.key"
            :class="{ 'sticky-col': col.sticky }"
        >
          <slot :name="col.key" :row="row">
            {{ row[col.key] }}
          </slot>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-wrapper {
  overflow-x: auto;
  width: 100%;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  white-space: nowrap;
  padding: 12px 15px;
  border-bottom: 1px solid #e0e0e0;
  border-right: 1px solid #e5e7eb;
  text-align: center;
}

th:last-child,
td:last-child {
  border-right: none;
}

/* Header */
thead th {
  background: #f1f5f9;
  font-weight: 700;
}

/* Hover */
tbody tr:hover {
  background: #f9fafb;
}

/* Sticky column */
.sticky-col {
  position: sticky;
  right: 0;
  background: white;
  z-index: 10;
}

/* Color riêng cho sticky column */
th.sticky-col {
  background-color: #e2f0ff !important;
  color: #003e7e;
}

td.sticky-col {
  background-color: #f5faff;
  border-left: 2px solid #d0e7ff;
}

tbody tr:hover td.sticky-col {
  background-color: #eaf4ff;
}
</style>
