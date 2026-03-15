import { createApp } from 'vue'
import { createVuestic } from 'vuestic-ui'
import 'vuestic-ui/css'
import App from './App.vue'
import { router } from './router'
import './styles.css'

createApp(App).use(router).use(createVuestic()).mount('#app')
