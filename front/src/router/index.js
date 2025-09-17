import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '@/views/HomePage.vue';
import PageLogin from '@/views/PageLogin.vue';
import PageLogout from '@/components/PageLogout.vue';
import PageTerm from '@/views/PageTerm.vue';
import PageRegister from '@/views/PageRegister.vue'
import NotFound from '@/components/NotFound.vue';
import BookInfo from '@/components/BookInfo.vue';
import AdminPage from '@/views/AdminPage.vue';
import BookBorrow from '@/components/BookBorrow.vue';
import BookReturn from '@/components/BookReturn.vue';
import TermPage1 from '@/components/TermPage1.vue';
import TermPage3 from '@/components/TermPage3.vue';
import TermPage2 from '@/components/TermPage2.vue';
import MyPage from '@/views/MyPage.vue';

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
    path: '/users',
    name: 'Mypage',
    component: MyPage
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
    path: '/borrow',
    name: 'BookBorrow',
    component: BookBorrow,
  },
  {
    path: '/return',
    name: 'BookReturn',
    component: BookReturn,
  },
  {
    path: '/service/terms',
    name: 'AgreeTermsUser',
    component: TermPage1
  },
  {
    path: '/service/info',
    name: 'AgreeInfoUser',
    component: TermPage2
  },
  {
    path: '/service/alarm',
    name: 'AgreeDiscordUser',
    component: TermPage3
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
