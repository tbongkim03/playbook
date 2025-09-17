import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '@/views/HomePage.vue';
import BooksLayout from '@/views/BooksLayout.vue';
import BookRegister from '@/views/BookRegister.vue';
import BooksTable from '@/components/BooksTable.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage
  }, {
    path: '/books',
    component: BooksLayout,
    children: [
      {
        path: '',
        name: 'BooksTable',
        component: BooksTable
      },
      {
        path: 'register',
        name: 'BookRegister',
        component: BookRegister
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
