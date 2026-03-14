import { createApp } from 'vue'
import App from './src/App.vue'
import { router } from './src/router'
import './src/styles.css'

createApp(App).use(router).mount('#app')