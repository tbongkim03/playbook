import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '@/views/HomePage.vue';
import BooksLayout from '@/views/BooksLayout.vue';
import BookRegister from '@/views/BookRegister.vue';
import BooksTable from '@/components/BooksTable.vue';
import PageLogin from '@/views/PageLogin.vue';
import PageLogout from '@/components/PageLogout.vue';
import PageTerm from '@/views/PageTerm.vue';
import PageRegister from '@/views/PageRegister.vue'
import NotFound from '@/components/NotFound.vue';
import BookArea from '@/components/BookArea.vue';
import BookInfo from '@/components/BookInfo.vue';
import AdminPage from '@/views/AdminPage.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage
  },
  {
    path: '/login',
    name: 'PageLogin',
    component: PageLogin
  },
  {
    path: '/logout',
    name: 'PageLogout',
    component: PageLogout
  },
  {
    path: '/terms',
    name: 'PageTerm',
    component: PageTerm
  },
  {
    path: '/register',
    name: 'PageRegister',
    component: PageRegister
  },
  {
    path: '/admin',
    name: 'AdminPage',
    component: AdminPage
  },
  {
    path: '/books/info/:id',
    name: 'BookInfo',
    component: BookInfo,
  },
  { 
    path: '/:catchAll(.*)',
    name: 'NotFound', 
    component:  NotFound
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
