<!-- BooksTable.vue -->
<template>
  <div class="book-management-container">
    <!-- 바코드 및 프린트 모달 -->
    <Barcode 
      :seqBook="selectedSeqBook"
      :seqSortSecond="selectedSeqSortSecond"
      :cntBook="selectedCntBook"
      :barcodeBook="selectedBarcode" 
      :titleBook="selectedBookTitle" 
      :isOpen="isOpen" 
      @close="isOpen = false"
    />

    <PrintBatch 
      v-if="isPrintBatchOpen" 
      :books="booksToPrint" 
      @close="isPrintBatchOpen = false"
    />

    <!-- 헤더 영역 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
              <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            도서 관리
          </h1>
          <p class="page-subtitle">도서 등록, 수정, 삭제 및 바코드 관리</p>
        </div>
        
        <div class="header-actions">
          <button type="button" class="register-btn" @click="$emit('open-register-modal')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="12" y1="8" x2="12" y2="16" stroke="currentColor" stroke-width="2"/>
              <line x1="8" y1="12" x2="16" y2="12" stroke="currentColor" stroke-width="2"/>
            </svg>
            도서 등록
          </button>
        </div>
      </div>
    </div>

    <!-- 필터 및 검색 영역 개선 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-content">
          <!-- 첫 번째 줄: 검색 및 필터 -->
          <div class="filter-row primary-filters">
            <div class="filter-group search-group">
              <label class="filter-label">검색</label>
              <div class="search-input-wrapper">
                <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                  <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
                </svg>
                <input 
                  type="text" 
                  v-model="filters.searchQuery"
                  placeholder="제목, 저자, 출판사, ISBN으로 검색..."
                  class="search-input"
                  @keyup.enter="$event.target.blur()"
                />
                <button 
                  v-if="filters.searchQuery"
                  @click="filters.searchQuery = ''"
                  class="clear-search-btn"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
                    <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
            </div>

            <div class="filter-group">
              <label class="filter-label">대분류</label>
              <select v-model="filters.categoryLarge" class="filter-select">
                <option value="">전체</option>
                <option
                  v-for="category in largeCategories"
                  :key="category.seqSortFirst"
                  :value="category.seqSortFirst"
                >
                  {{ category.korSortFirst }}
                </option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">중분류</label>
              <select 
                v-model="filters.categoryMedium" 
                class="filter-select"
                :disabled="!filters.categoryLarge"
              >
                <option value="">전체</option>
                <option
                  v-for="category in availableMediumCategories"
                  :key="category.seqSortSecond"
                  :value="category.seqSortSecond"
                >
                  {{ category.korSortSecond }}
                </option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">대출상태</label>
              <select v-model="filters.borrowStatus" class="filter-select">
                <option value="">전체</option>
                <option value="borrowed">대출 중</option>
                <option value="available">대출 가능</option>
                <option value="unavailable">대출 불가</option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">정렬</label>
              <select v-model="filters.sortBy" class="filter-select">
                <option value="title_asc">제목 가나다순</option>
                <option value="title_desc">제목 역순</option>
                <option value="author_asc">저자 가나다순</option>
                <option value="author_desc">저자 역순</option>
                <option value="publisher_asc">출판사 가나다순</option>
                <option value="publisher_desc">출판사 역순</option>
                <option value="date_desc">출판일 최신순</option>
                <option value="date_asc">출판일 오래된순</option>
              </select>
            </div>
            
            <!-- 등록일 필터 -->
            <div class="filter-group">
              <label class="filter-label">등록일</label>
              <DateRangePicker ref="datePickerRef" @change="onDateRangeChange" />
            </div>

            <!-- 캠퍼스 필터 (전체 관리자만 표시) -->
            <div v-if="showCampusFilter" class="filter-group">
              <label class="filter-label">캠퍼스</label>
              <select v-model="filters.campus" @change="applyFilters" class="filter-select">
                <option value="">전체 캠퍼스</option>
                <option
                  v-for="campus in campuses"
                  :key="campus.seqCampus"
                  :value="campus.seqCampus"
                >
                  {{ campus.nameCampus }}
                </option>
              </select>
            </div>
          </div>
          
          <!-- 두 번째 줄: 액션 버튼들 -->
          <div class="filter-row action-controls">
            <div class="control-group">
              <button @click="resetFilters" class="reset-filters-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 3V7M12 17V21M21 12H17M7 12H3" stroke="currentColor" stroke-width="2"/>
                </svg>
                초기화
              </button>
            </div>
            
            <div class="print-controls">
              <div class="print-toggle">
                <label class="toggle-label">
                  <input 
                    type="checkbox" 
                    v-model="isPrint" 
                    class="toggle-input"
                  />
                  <span class="toggle-slider"></span>
                  <span class="toggle-text">프린트 모드</span>
                </label>
              </div>
              
              <div v-if="isPrint" class="print-selection-info">
                <span class="selection-count">
                  선택: <strong>{{ selectedBooks.size }}</strong> / {{ MAX_SELECTION }}개
                </span>
              </div>
              <button 
                v-if="isPrint" 
                @click="printBarcodes" 
                class="batch-print-btn"
                :disabled="selectedBooks.size === 0"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <polyline points="6,9 6,2 18,2 18,9" stroke="currentColor" stroke-width="2"/>
                  <path d="M6,18H4C3.46957,18 2.96086,17.7893 2.58579,17.4142C2.21071,17.0391 2,16.5304 2,16V11C2,10.4696 2.21071,9.96086 2.58579,9.58579C2.96086,9.21071 3.46957,9 4,9H20C20.5304,9 21.0391,9.21071 21.4142,9.58579C21.7893,9.96086 22,10.4696 22,11V16C22,16.5304 21.7893,17.0391 21.4142,17.4142C21.0391,17.7893 20.5304,18 20,18H18" stroke="currentColor" stroke-width="2"/>
                  <rect x="6" y="14" width="12" height="8" stroke="currentColor" stroke-width="2"/>
                </svg>
                일괄 출력
                <span class="count-badge">{{ selectedBooks.size }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 통계 정보 -->
    <div class="stats-section">
      <div class="stat-card total-books">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
            <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ isPrint ? filteredBooks.length : totalCount }}</div>
          <div class="stat-label">표시된 도서</div>
        </div>
      </div>
      
      <div class="stat-card total-books">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
            <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"/>
            <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"/>
            <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ allBooks.length }}</div>
          <div class="stat-label">전체 도서</div>
        </div>
      </div>

      <div class="stat-card borrowed-books">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M16 4H18C18.5304 4 19.0391 4.21071 19.4142 4.58579C19.7893 4.96086 20 5.46957 20 6V18C20 18.5304 19.7893 19.0391 19.4142 19.4142C19.0391 19.7893 18.5304 20 18 20H6C5.46957 20 4.96086 19.7893 4.58579 19.4142C4.21071 19.0391 4 18.5304 4 18V6C4 5.46957 4.21071 4.96086 4.58579 4.58579C4.96086 4.21071 5.46957 4 6 4H8" stroke="currentColor" stroke-width="2"/>
            <rect x="8" y="2" width="8" height="4" rx="1" ry="1" stroke="currentColor" stroke-width="2"/>
            <line x1="8" y1="12" x2="16" y2="12" stroke="currentColor" stroke-width="2"/>
            <line x1="8" y1="16" x2="12" y2="16" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ borrowedCount }}</div>
          <div class="stat-label">대출 중</div>
        </div>
      </div>

      <div class="stat-card available-books">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <polyline points="20,6 9,17 4,12" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ availableCount }}</div>
          <div class="stat-label">대출 가능</div>
        </div>
      </div>

      <div class="stat-card unavailable-books">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
            <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ unavailableCount }}</div>
          <div class="stat-label">대출 불가</div>
        </div>
      </div>

      <div v-if="isPrint" class="stat-card print-ready">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <polyline points="6,9 6,2 18,2 18,9" stroke="currentColor" stroke-width="2"/>
            <path d="M6,18H4C3.46957,18 2.96086,17.7893 2.58579,17.4142C2.21071,17.0391 2,16.5304 2,16V11C2,10.4696 2.21071,9.96086 2.58579,9.58579C2.96086,9.21071 3.46957,9 4,9H20C20.5304,9 21.0391,9.21071 21.4142,9.58579C21.7893,9.96086 22,10.4696 22,11V16C22,16.5304 21.7893,17.0391 21.4142,17.4142C21.0391,17.7893 20.5304,18 20,18H18" stroke="currentColor" stroke-width="2"/>
            <rect x="6" y="14" width="12" height="8" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ booksToPrint.length }}</div>
          <div class="stat-label">출력 대기</div>
        </div>
      </div>
    </div>

    <!-- 도서 테이블 -->
    <div class="table-section">
      <div class="table-card">
        <div class="table-header">
          <h3>도서 목록</h3>
          <div class="table-actions">
            <button class="export-btn" @click="exportData">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2"/>
                <polyline points="7,10 12,15 17,10" stroke="currentColor" stroke-width="2"/>
                <line x1="12" y1="15" x2="12" y2="3" stroke="currentColor" stroke-width="2"/>
              </svg>
              엑셀로 내보내기
            </button>
            <button
              @click="refreshBooks"
              class="refresh-btn"
              :disabled="isRefreshing"
              title="목록 새로고침"
            >
              <svg 
                width="16" 
                height="16" 
                viewBox="0 0 24 24" 
                fill="none" 
                xmlns="http://www.w3.org/2000/svg"
                :class="{ 'spinning': isRefreshing }"
              >
                <path d="M3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12Z" stroke="currentColor" stroke-width="2"/>
                <path d="M12 3V7M12 17V21M21 12H17M7 12H3" stroke="currentColor" stroke-width="2"/>
              </svg>
              {{ isRefreshing ? '새로고침 중...' : '새로고침' }}
            </button>
            <span class="result-count">{{ paginatedBooks.length }}개 표시 (페이지 {{ currentPage }}/{{ totalPages }})</span>
          </div>
        </div>
        
        <div class="table-wrapper">
          <table class="books-table">
            <thead>
              <tr>
                <th v-if="isPrint" class="col-checkbox">
                  <input 
                    type="checkbox" 
                    :checked="isAllSelectedOnCurrentPage"
                    :indeterminate="isSomeSelectedOnCurrentPage && !isAllSelectedOnCurrentPage"
                    @change="toggleAllOnCurrentPage"
                    @click.stop
                    class="checkbox-input"
                  />
                </th>
                <th class="col-title">제목</th>
                <th class="col-isbn">ISBN</th>
                <th class="col-author">저자</th>
                <th class="col-publisher">출판사</th>
                <th class="col-date">출판일</th>
                <th class="col-category">대분류</th>
                <th class="col-category">중분류</th>
                <th class="col-count">번호</th>
                <th class="col-status">대출상태</th>
                <th class="col-barcode">바코드</th>
                <th class="col-actions">작업</th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="book in paginatedBooks" 
                :key="book.seqBook" 
                :class="[
                  'book-row', 
                  { 
                    'active-row': activeRowId === book.seqBook,
                    'selected-row': isPrint && selectedBooks.has(book.seqBook),
                    'selectable-row': isPrint && canSelectBook(book)
                  }
                ]"
                @click="isPrint ? handleRowClick(book, $event) : setActiveRow(book.seqBook)"
                @mousedown="isPrint ? handleMouseDown(book, $event) : null"
                @mouseenter="isPrint ? handleMouseEnter(book, $event) : null"
                @mouseup="isPrint ? handleMouseUp() : null"
                @mouseleave="isPrint && isDragging ? null : null"
              >
                <td v-if="isPrint" class="col-checkbox" @click.stop>
                  <input 
                    type="checkbox" 
                    :checked="selectedBooks.has(book.seqBook)"
                    :disabled="!canSelectBook(book)"
                    @change="toggleBookSelection(book)"
                    @click.stop
                    class="checkbox-input"
                  />
                </td>
                <td class="book-title col-title">
                  <div class="title-content">
                    <span class="title-text" :title="book.titleBook">{{ book.titleBook }}</span>
                  </div>
                </td>
                <td class="isbn col-isbn" :title="book.isbnBook">{{ book.isbnBook }}</td>
                <td class="author col-author" :title="book.authorBook">{{ book.authorBook }}</td>
                <td class="publisher col-publisher" :title="book.publisherBook">{{ book.publisherBook }}</td>
                <td class="publish-date col-date">{{ formatDate(book.publishDateBook) }}</td>
                <td class="category-large col-category">
                  <select 
                    class="category-select" 
                    v-model="book.categoryLarge"
                    @click="setActiveRow(book.seqBook)"
                  >
                    <option
                      v-for="category in largeCategories"
                      :key="category.nameSortFirst"
                      :value="category.nameSortFirst"
                    >
                      {{ category.korSortFirst }}
                    </option>
                  </select>
                </td>
                <td class="category-medium col-category">
                  <select
                    class="category-select"
                    v-model="book.categoryMedium"
                    :disabled="!book.mediumOptions.length"
                    @click="setActiveRow(book.seqBook)"
                  >
                    <option
                      v-for="(category, index) in book.mediumOptions"
                      :key="index"
                      :value="category.seqSortSecond"
                    >
                      {{ category.korSortSecond }}
                    </option>
                  </select>
                </td>
                <td class="book-count col-count">
                  <input 
                    type="number" 
                    class="count-input" 
                    v-model="book.cntBook" 
                    min="1" 
                    @input="() => { if (book.cntBook < 1) book.cntBook = 1 }"
                    @click="setActiveRow(book.seqBook)"
                  />
                </td>
                <td class="borrow-status col-status">
                  <span :class="[
                    'status-badge',
                    getBookStatusClass(book)
                  ]">
                    {{ getBookStatusText(book) }}
                  </span>
                </td>
                <td class="barcode col-barcode">
                  <div class="barcode-display">
                    <input 
                      type="text" 
                      class="barcode-input" 
                      v-model="book.barcodeBook" 
                      readonly
                      :title="book.barcodeBook"
                    />
                  </div>
                </td>
                <td class="actions col-actions">
                  <div class="action-buttons">
                    <button @click="barcodeCreate(book)" class="action-btn barcode-btn" title="바코드 생성">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="9" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="13" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="17" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </button>
                    <button @click="deleteBook(book)" class="action-btn delete-btn" title="도서 삭제">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <polyline points="3,6 5,6 21,6" stroke="currentColor" stroke-width="2"/>
                        <path d="M19,6V20C19,20.5304 18.7893,21.0391 18.4142,21.4142C18.0391,21.7893 17.5304,22 17,22H7C6.46957,22 5.96086,21.7893 5.58579,21.4142C5.21071,21.0391 5,20.5304 5,20V6M8,6V4C8,3.46957 8.21071,2.96086 8.58579,2.58579C8.96086,2.21071 9.46957,2 10,2H14C14.5304,2 15.0391,2.21071 15.4142,2.58579C15.7893,2.96086 16,3.46957 16,4V6" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 빈 상태 -->
          <div v-if="paginatedBooks.length === 0" class="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
              <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h3>도서가 없습니다</h3>
            <p>조건에 맞는 도서가 없습니다. 필터를 초기화하거나 새로운 도서를 등록해보세요.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div class="gl-pagination" v-if="totalPages > 1">
      <span class="gl-pagination-info">{{ paginationInfo }}</span>
      <nav class="gl-pagination-nav">
        <button
          class="gl-page-btn prev-btn"
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          이전
        </button>
        <template v-for="item in paginationItems" :key="item + '-bt'">
          <span v-if="item === '...'" class="gl-page-ellipsis">…</span>
          <button
            v-else
            class="gl-page-btn"
            :class="{ active: item === currentPage }"
            @click="goToPage(item)"
          >{{ item }}</button>
        </template>
        <button
          class="gl-page-btn next-btn"
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          다음
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </button>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, watchEffect } from 'vue'
import * as bookApi from '@/api/book'
import * as sortApi from '@/api/sort'
import * as campusApi from '@/api/campus'
import * as adminApi from '@/api/admin'
import Barcode from './Barcode.vue'
import PrintBatch from './BookPrintBatch.vue'
import DateRangePicker from './DateRangePicker.vue'
import { swAlert, swConfirm } from '@/utils/sweetAlert'
import { MAX_BARCODE_SELECTION } from '@/utils/constants'
import { exportToXlsx } from '@/utils/exportSheet'

// emit 정의
defineEmits(['open-register-modal'])


// 데이터 상태
const allBooks = ref([])
const largeCategories = ref([])
const mediumCategoriesAll = ref([])
const isOpen = ref(false)
const selectedBarcode = ref('')
const selectedBookTitle = ref('')
const selectedSeqBook = ref('')
const selectedSeqSortSecond = ref('')
const selectedCntBook = ref('')
const isPrint = ref(false)
const isPrintBatchOpen = ref(false)
const isRefreshing = ref(false)
const activeRowId = ref(null)

// 프린트 모드 선택 상태
const selectedBooks = ref(new Set()) // seqBook을 저장
const isDragging = ref(false)
const dragStartBook = ref(null)
const MAX_SELECTION = MAX_BARCODE_SELECTION

// 필터 상태
const filters = ref({
  searchQuery: '',
  categoryLarge: '',
  categoryMedium: '',
  borrowStatus: '',
  sortBy: 'title_asc',
  campus: '' // 캠퍼스 필터
})

// 날짜 필터 상태
const datePickerRef = ref(null)
const registerStartDate = ref('')
const registerEndDate = ref('')

const onDateRangeChange = (range) => {
  registerStartDate.value = range.startDate || ''
  registerEndDate.value = range.endDate || ''
  fetchAdminBooks(1)
}

// 캠퍼스 필터 관련
const campuses = ref([])
const showCampusFilter = ref(false)
const currentUserCampusId = ref(null)

// 페이지네이션 상태
const currentPage = ref(1)
const pageSize = 15

// 서버사이드 페이지 데이터
const pagedBooks = ref([])
const totalCount = ref(0)
const isLoadingBooks = ref(false)

// 키보드 이벤트 핸들러
const handleKeydown = (event) => {
  if (event.key === 'Escape' && isOpen.value) {
    isOpen.value = false
  }

  if (event.key === 'Escape' && isPrintBatchOpen.value) {
    isPrintBatchOpen.value = false
  }
}

// 도서 상태 관련 함수들
const getBookStatus = (book) => {
  // 바코드가 출력되지 않았으면 대출 불가
  if (!book.printCheckBook) {
    return 'unavailable'
  }
  
  // 바코드가 출력되었고 대출 중이면 대출 중
  if (book.bookBorrowed) {
    return 'borrowed'
  }
  
  // 바코드가 출력되었고 대출 중이 아니면 대출 가능
  return 'available'
}

const getBookStatusText = (book) => {
  const status = getBookStatus(book)
  switch (status) {
    case 'borrowed':
      return '대출 중'
    case 'available':
      return '대출 가능'
    case 'unavailable':
      return '대출 불가'
    default:
      return '대출 불가'
  }
}

const getBookStatusClass = (book) => {
  const status = getBookStatus(book)
  switch (status) {
    case 'borrowed':
      return 'status-borrowed'
    case 'available':
      return 'status-available'
    case 'unavailable':
      return 'status-unavailable'
    default:
      return 'status-unavailable'
  }
}

// 대분류 데이터 가져오기
const fetchLargeCategories = async () => {
  const res = await sortApi.getFirstCategories()
  largeCategories.value = res.data.data
}

// 중분류 데이터 가져오기
const fetchMediumCategories = async () => {
  const res = await sortApi.getSecondCategories()
  mediumCategoriesAll.value = res.data.data
}

// 캠퍼스 목록 가져오기
const fetchCampuses = async () => {
  try {
    const res = await campusApi.getAll()
    campuses.value = res.data.data || []
  } catch (error) {
    console.error('캠퍼스 목록 조회 실패:', error)
  }
}

// 사용자 타입 확인 및 캠퍼스 필터 설정
const checkUserType = async () => {
  try {
    if (!sessionStorage.getItem('userType')) return

    const response = await adminApi.checkMe()

    if (response.status === 200) {
      const data = response.data.data
      if (!data.seqCampus) {
        // 전체 관리자
        showCampusFilter.value = true
        currentUserCampusId.value = null
      } else {
        // 특정 캠퍼스 관리자
        showCampusFilter.value = false
        currentUserCampusId.value = data.seqCampus.seqCampus || data.seqCampus
        filters.value.campus = String(currentUserCampusId.value) // 기본값 설정
      }
    }
  } catch (error) {
    console.error('사용자 타입 확인 실패:', error)
  }
}

// seqSortSecond(중분류 시퀀스)로 중분류 정보 찾기
const findMediumCategory = (seqSecond) =>
  mediumCategoriesAll.value.find(m => m.seqSortSecond === seqSecond)

// seqSortSecond(중분류 시퀀스)로 대분류 nameSortFirst 찾기
const findLargeCodeFromSeqSecond = (seqSecond) => {
  const medium = findMediumCategory(seqSecond)
  if (!medium) return ''
  
  const large = largeCategories.value.find(l => l.seqSortFirst === medium.seqSortFirst)
  return large?.nameSortFirst || ''
}

// 특정 대분류 seqSortFirst에 해당하는 중분류 옵션들
const getMediumOptions = (largeSeq) => {
  if (!largeSeq) return []
  return mediumCategoriesAll.value.filter(m => m.seqSortFirst === largeSeq)
}

// 선택된 대분류에 따른 중분류 옵션
const availableMediumCategories = computed(() => {
  if (!filters.value.categoryLarge) return []
  return getMediumOptions(filters.value.categoryLarge)
})

// 대여 상태별 통계 - 개선된 로직
const borrowedCount = computed(() => 
  allBooks.value.filter(book => getBookStatus(book) === 'borrowed').length
)

const availableCount = computed(() => 
  allBooks.value.filter(book => getBookStatus(book) === 'available').length
)

const unavailableCount = computed(() => 
  allBooks.value.filter(book => getBookStatus(book) === 'unavailable').length
)

// 모든 도서 데이터 가져오기 (페이지네이션 없이)
const fetchBooks = async () => {
  const res = await bookApi.getAllForAdmin()
  const data = res.data.data

  if (!Array.isArray(data)) {
    allBooks.value = []
    return
  }

  allBooks.value = data.map(book => {
    const largeCode = findLargeCodeFromSeqSecond(book.seqSortSecond)
    const mediumOptions = getMediumOptions(largeCode)
    return {
      ...book,
      categoryLarge: largeCode,
      categoryMedium: book.seqSortSecond ?? '',
      mediumOptions
    }
  })
}

// 정렬 파라미터 변환
const parseSortBy = (sortByValue) => {
  const map = {
    'title_asc':      { sortBy: 'titleBook',       sortDir: 'asc'  },
    'title_desc':     { sortBy: 'titleBook',       sortDir: 'desc' },
    'author_asc':     { sortBy: 'authorBook',      sortDir: 'asc'  },
    'author_desc':    { sortBy: 'authorBook',      sortDir: 'desc' },
    'publisher_asc':  { sortBy: 'publisherBook',   sortDir: 'asc'  },
    'publisher_desc': { sortBy: 'publisherBook',   sortDir: 'desc' },
    'date_desc':      { sortBy: 'publishDateBook', sortDir: 'desc' },
    'date_asc':       { sortBy: 'publishDateBook', sortDir: 'asc'  },
  }
  return map[sortByValue] || { sortBy: 'seqBook', sortDir: 'desc' }
}

// 서버사이드 도서 조회
const fetchAdminBooks = async (page = 1) => {
  if (isPrint.value) return  // 프린트 모드는 기존 방식 유지
  try {
    isLoadingBooks.value = true
    const { sortBy, sortDir } = parseSortBy(filters.value.sortBy)
    const params = {
      page,
      size: pageSize,
      sortBy,
      sortDir,
    }
    if (filters.value.campus) params.campusId = filters.value.campus
    else if (currentUserCampusId.value) params.campusId = currentUserCampusId.value

    if (filters.value.searchQuery.trim()) params.search = filters.value.searchQuery.trim()
    if (filters.value.categoryLarge !== '') params.seqSortFirst = filters.value.categoryLarge
    if (filters.value.categoryMedium !== '') params.seqSortSecond = filters.value.categoryMedium
    if (filters.value.borrowStatus) params.borrowStatus = filters.value.borrowStatus

    if (registerStartDate.value) params.registerStartDate = registerStartDate.value
    if (registerEndDate.value) params.registerEndDate = registerEndDate.value

    const res = await bookApi.getAdminList(params)
    const data = res.data.data
    pagedBooks.value = (data.content || []).map(book => {
      const largeCode = findLargeCodeFromSeqSecond(book.seqSortSecond)
      const mediumOptions = getMediumOptions(largeCode)
      return { ...book, categoryLarge: largeCode, categoryMedium: book.seqSortSecond ?? '', mediumOptions }
    })
    totalCount.value = data.totalCount || 0
    currentPage.value = page
  } catch (error) {
    console.error('도서 목록 조회 실패:', error)
  } finally {
    isLoadingBooks.value = false
  }
}

// 한글 문자열 비교를 위한 함수
const compareKorean = (a, b) => {
  return a.localeCompare(b, 'ko-KR')
}

// 필터링된 도서 목록 - 개선된 상태 필터링
const filteredBooks = computed(() => {
  let result = [...allBooks.value]

  // 검색 필터
  if (filters.value.searchQuery.trim()) {
    const query = filters.value.searchQuery.trim().toLowerCase()
    result = result.filter(book => 
      book.titleBook?.toLowerCase().includes(query) ||
      book.authorBook?.toLowerCase().includes(query) ||
      book.publisherBook?.toLowerCase().includes(query) ||
      book.isbnBook?.toLowerCase().includes(query)
    )
  }

  // 대분류 필터
  if (filters.value.categoryLarge !== '') {  // 빈 문자열이 아니면 필터링
    result = result.filter(book => {
      const large = largeCategories.value.find(l => l.seqSortFirst === filters.value.categoryLarge)
      if (!large) return false
      return book.categoryLarge === large.nameSortFirst
    })
  }

  // 중분류 필터  
  if (filters.value.categoryMedium !== '') {  // 빈 문자열이 아니면 필터링
    result = result.filter(book => book.categoryMedium === filters.value.categoryMedium)
  }

  // 대여 상태 필터 - 개선된 로직
  if (filters.value.borrowStatus) {
    result = result.filter(book => getBookStatus(book) === filters.value.borrowStatus)
  }

  // 캠퍼스 필터 (전체 관리자가 다른 캠퍼스를 선택한 경우)
  if (showCampusFilter.value && filters.value.campus) {
    result = result.filter(book => book.seqCampus === parseInt(filters.value.campus))
  }

  // 프린트 모드 필터
  if (isPrint.value) {
    result = result.filter(book => book.printCheckBook === false)
  }

  // 정렬
  result.sort((a, b) => {
    switch (filters.value.sortBy) {
      case 'title_asc':
        return compareKorean(a.titleBook || '', b.titleBook || '')
      case 'title_desc':
        return compareKorean(b.titleBook || '', a.titleBook || '')
      case 'author_asc':
        return compareKorean(a.authorBook || '', b.authorBook || '')
      case 'author_desc':
        return compareKorean(b.authorBook || '', a.authorBook || '')
      case 'publisher_asc':
        return compareKorean(a.publisherBook || '', b.publisherBook || '')
      case 'publisher_desc':
        return compareKorean(b.publisherBook || '', a.publisherBook || '')
      case 'date_desc':
        return new Date(b.publishDateBook || 0) - new Date(a.publishDateBook || 0)
      case 'date_asc':
        return new Date(a.publishDateBook || 0) - new Date(b.publishDateBook || 0)
      default:
        return 0
    }
  })

  return result
})

// 페이지네이션 계산
const totalPages = computed(() =>
  isPrint.value
    ? Math.ceil(filteredBooks.value.length / pageSize)
    : Math.ceil(totalCount.value / pageSize)
)

const paginatedBooks = computed(() => {
  if (isPrint.value) {
    const start = (currentPage.value - 1) * pageSize
    const end = start + pageSize
    return filteredBooks.value.slice(start, end)
  }
  return pagedBooks.value
})

const paginationItems = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const items = [1]
  if (current > 3) items.push('...')
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  for (let i = start; i <= end; i++) items.push(i)
  if (current < total - 2) items.push('...')
  items.push(total)
  return items
})

const paginationInfo = computed(() => {
  const total = isPrint.value ? filteredBooks.value.length : totalCount.value
  const start = (currentPage.value - 1) * pageSize + 1
  const end = Math.min(currentPage.value * pageSize, total)
  return `${start}–${end} / 전체 ${total}건`
})

// 선택 가능한 도서인지 확인 (미출력이고 분류가 완료된 도서)
const canSelectBook = (book) => {
  return book.printCheckBook === false &&
    book.categoryLarge !== 0 &&
    book.categoryMedium !== 0 &&
    book.barcodeBook &&
    book.barcodeBook.trim() !== ''
}

// 프린트할 도서 목록 - 선택된 도서들만
const booksToPrint = computed(() => {
  return filteredBooks.value.filter(book => 
    selectedBooks.value.has(book.seqBook) && canSelectBook(book)
  )
})

// 현재 페이지의 모든 도서가 선택되었는지
const isAllSelectedOnCurrentPage = computed(() => {
  const selectableBooks = paginatedBooks.value.filter(canSelectBook)
  if (selectableBooks.length === 0) return false
  return selectableBooks.every(book => selectedBooks.value.has(book.seqBook))
})

// 현재 페이지의 일부 도서가 선택되었는지
const isSomeSelectedOnCurrentPage = computed(() => {
  const selectableBooks = paginatedBooks.value.filter(canSelectBook)
  return selectableBooks.some(book => selectedBooks.value.has(book.seqBook))
})

// 필터 초기화
const resetFilters = () => {
  filters.value = {
    searchQuery: '',
    categoryLarge: '',
    categoryMedium: '',
    borrowStatus: '',
    campus: showCampusFilter.value ? '' : (currentUserCampusId.value ? String(currentUserCampusId.value) : ''),
    sortBy: 'title_asc'
  }
  registerStartDate.value = ''
  registerEndDate.value = ''
  datePickerRef.value?.reset()
  currentPage.value = 1
  fetchAdminBooks(1)
}

// 대분류 변경 시 중분류 초기화
watchEffect(() => {
  if (filters.value.categoryLarge === '') {
    filters.value.categoryMedium = ''
  } else {
    const availableOptions = availableMediumCategories.value
    if (filters.value.categoryMedium !== '' && !availableOptions.find(opt => opt.seqSortSecond === filters.value.categoryMedium)) {
      filters.value.categoryMedium = ''
    }
  }
})

// 필터 변경 시 서버 재조회 (검색은 디바운스)
let searchTimer = null
watch(() => filters.value.searchQuery, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => fetchAdminBooks(1), 400)
})

watch([
  () => filters.value.categoryLarge,
  () => filters.value.categoryMedium,
  () => filters.value.borrowStatus,
  () => filters.value.sortBy,
  () => filters.value.campus,
], () => fetchAdminBooks(1))

watch([registerStartDate, registerEndDate], () => {
  fetchAdminBooks(1)
})

// 각 book의 categoryLarge가 바뀔 때 개별 감시 (인라인 편집용)
watchEffect(() => {
  pagedBooks.value.forEach(book => {
    const largeCode = book.categoryLarge
    const oldOptions = book.mediumOptions?.map(m => m.seqSortSecond) || []

    // 대분류가 없으면 중분류 비움
    if (!largeCode) {
      if (book.categoryMedium !== '') {
        book.categoryMedium = ''
      }
      book.mediumOptions = []
    } else {
      // 대분류에 맞는 중분류 옵션 - nameSortFirst로 찾기
      const large = largeCategories.value.find(l => l.nameSortFirst === largeCode)
      if (!large) {
        book.mediumOptions = []
        book.categoryMedium = ''
        return
      }
      
      const newOptions = mediumCategoriesAll.value.filter(m => m.seqSortFirst === large.seqSortFirst)
      const newOptionsIds = newOptions.map(m => m.seqSortSecond)

      // 옵션이 변경되었을 때만 업데이트
      if (JSON.stringify(oldOptions) !== JSON.stringify(newOptionsIds)) {
        book.mediumOptions = newOptions

        // 현재 선택된 중분류가 새 옵션에 없으면 초기화
        if (!newOptionsIds.includes(book.categoryMedium)) {
          book.categoryMedium = newOptions.length > 0 ? newOptions[0].seqSortSecond : ''
        }
      }
    }

    // 바코드 자동 생성 기능
    const large = largeCategories.value.find(l => l.nameSortFirst === book.categoryLarge)
    const medium = mediumCategoriesAll.value.find(m => m.seqSortSecond === book.categoryMedium)

    if (large && medium) {
      const isbn = book.isbnBook
      const cnt = book.cntBook

      if (isbn && cnt && book.categoryLarge && book.categoryMedium !== '') {
        const barcode = `${large.nameSortFirst}${medium.nameSortSecond}-${isbn}-${cnt}`
        if (book.barcodeBook !== barcode) {
          book.barcodeBook = barcode
        }
      }
    }
  })
})

// 페이지 이동 (서버 재조회 포함)
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    if (isPrint.value) {
      currentPage.value = page
    } else {
      fetchAdminBooks(page)
    }
  }
}

// 마운트 시 데이터 로드
onMounted(async () => {
  await fetchLargeCategories()
  await fetchMediumCategories()
  await fetchCampuses()
  await checkUserType()
  await fetchBooks()         // stats + print mode용
  await fetchAdminBooks(1)  // 테이블 서버사이드
  window.addEventListener('keydown', handleKeydown)
  // 드래그 중 마우스가 테이블 밖으로 나갔을 때 처리
  window.addEventListener('mouseup', handleMouseUp)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('mouseup', handleMouseUp)
})

// 활성 행 설정
const setActiveRow = (seqBook) => {
  activeRowId.value = seqBook
}

// 도서 삭제
async function deleteBook(book) {
  if (!(await swConfirm(`"${book.titleBook}" 도서를 삭제하시겠습니까?`, '', { isDangerous: true }))) {
    return
  }

  try {
    setActiveRow(book.seqBook)
    await bookApi.remove(book.seqBook)

    allBooks.value = allBooks.value.filter(b => b.seqBook !== book.seqBook)
    pagedBooks.value = pagedBooks.value.filter(b => b.seqBook !== book.seqBook)
    activeRowId.value = null
    await swAlert('삭제에 성공하였습니다.', 'success')
    await fetchAdminBooks(currentPage.value)
  } catch (error) {
    await swAlert(`삭제 실패: ${error.response?.data?.msg || error.message}`, 'error')
  }
}

// 바코드 생성
function barcodeCreate(book) {
  setActiveRow(book.seqBook) // 클릭 시 활성 행 설정
  selectedSeqBook.value = book.seqBook
  selectedSeqSortSecond.value = book.categoryMedium
  selectedCntBook.value = book.cntBook
  selectedBarcode.value = book.barcodeBook
  selectedBookTitle.value = book.titleBook
  isOpen.value = true
}

// 도서 선택 토글
async function toggleBookSelection(book) {
  if (!canSelectBook(book)) return

  if (selectedBooks.value.has(book.seqBook)) {
    selectedBooks.value.delete(book.seqBook)
  } else {
    if (selectedBooks.value.size >= MAX_SELECTION) {
      await swAlert(`최대 ${MAX_SELECTION}개까지 선택할 수 있습니다.`, 'warning')
      return
    }
    selectedBooks.value.add(book.seqBook)
  }
}

// 현재 페이지의 모든 선택 가능한 도서 선택/해제
async function toggleAllOnCurrentPage() {
  const selectableBooks = paginatedBooks.value.filter(canSelectBook)

  if (isAllSelectedOnCurrentPage.value) {
    selectableBooks.forEach(book => selectedBooks.value.delete(book.seqBook))
  } else {
    const remainingSlots = MAX_SELECTION - selectedBooks.value.size
    if (remainingSlots < selectableBooks.length) {
      await swAlert(`최대 ${MAX_SELECTION}개까지 선택할 수 있습니다. 현재 ${selectedBooks.value.size}개 선택됨.`, 'warning')
      return
    }
    // 모두 선택
    selectableBooks.forEach(book => {
      if (!selectedBooks.value.has(book.seqBook)) {
        selectedBooks.value.add(book.seqBook)
      }
    })
  }
}

// 드래그 선택 관련
let lastDraggedBook = null

function handleMouseDown(book, event) {
  if (!canSelectBook(book)) return
  if (event.button !== 0) return // 왼쪽 버튼만
  
  // 체크박스나 입력 요소를 클릭한 경우는 드래그 시작하지 않음
  if (event.target.type === 'checkbox' || 
      event.target.tagName === 'INPUT' || 
      event.target.tagName === 'SELECT' ||
      event.target.closest('input') ||
      event.target.closest('select') ||
      event.target.closest('button')) {
    return
  }
  
  isDragging.value = true
  dragStartBook.value = book
  lastDraggedBook = book
  event.preventDefault() // 텍스트 선택 방지
  
  // 드래그 시작 도서 선택 상태 토글
  toggleBookSelection(book)
}

function handleMouseEnter(book, event) {
  if (!isDragging.value || !dragStartBook.value) return
  if (!canSelectBook(book)) return
  if (lastDraggedBook?.seqBook === book.seqBook) return // 같은 행이면 무시
  
  lastDraggedBook = book
  
  // 드래그 시작 도서와 현재 도서 사이의 모든 도서 선택
  const startIndex = paginatedBooks.value.findIndex(b => b.seqBook === dragStartBook.value.seqBook)
  const endIndex = paginatedBooks.value.findIndex(b => b.seqBook === book.seqBook)
  
  if (startIndex === -1 || endIndex === -1) return
  
  const start = Math.min(startIndex, endIndex)
  const end = Math.max(startIndex, endIndex)
  
  const booksToSelect = paginatedBooks.value.slice(start, end + 1).filter(canSelectBook)
  
  // 드래그 시작 도서의 선택 상태에 따라 선택 또는 해제
  const shouldSelect = selectedBooks.value.has(dragStartBook.value.seqBook)
  
  booksToSelect.forEach(b => {
    if (shouldSelect) {
      if (selectedBooks.value.size < MAX_SELECTION) {
        selectedBooks.value.add(b.seqBook)
      }
    } else {
      selectedBooks.value.delete(b.seqBook)
    }
  })
}

function handleMouseUp() {
  if (isDragging.value) {
    isDragging.value = false
    dragStartBook.value = null
    lastDraggedBook = null
  }
}

// 행 클릭 처리 (프린트 모드일 때)
function handleRowClick(book, event) {
  // 체크박스, 입력 요소, 선택 요소를 클릭한 경우는 무시
  if (event.target.type === 'checkbox' || 
      event.target.tagName === 'INPUT' || 
      event.target.tagName === 'SELECT' ||
      event.target.closest('input') ||
      event.target.closest('select') ||
      event.target.closest('button')) {
    return
  }
  
  // 드래그가 아닌 단순 클릭인 경우에만 선택 토글
  if (!isDragging.value && canSelectBook(book)) {
    toggleBookSelection(book)
  }
}

// 프린트 모드 토글 시 선택 초기화
watchEffect(() => {
  if (!isPrint.value) {
    selectedBooks.value.clear()
  }
})

// 일괄 프린트
async function printBarcodes() {
  if (selectedBooks.value.size === 0) {
    await swAlert('출력할 도서를 선택해주세요.', 'warning')
    return
  }
  if (selectedBooks.value.size > MAX_SELECTION) {
    await swAlert(`최대 ${MAX_SELECTION}개까지 선택할 수 있습니다.`, 'warning')
    return
  }
  isPrintBatchOpen.value = true
}

// 날짜 포맷팅
function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

const exportData = async () => {
  try {
    const campusId = currentUserCampusId.value || null
    const res = await bookApi.exportExcel(campusId)
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = '도서목록.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 내보내기 실패:', e)
  }
}

// 도서 목록 새로고침
const refreshBooks = async () => {
  isRefreshing.value = true
  try {
    await fetchBooks()
    await fetchAdminBooks(currentPage.value)
  } catch (error) {
    await swAlert('목록을 새로고침하는 중 오류가 발생했습니다.', 'error')
  } finally {
    isRefreshing.value = false
  }
}
</script>

<style scoped>
/* ─── 레이아웃 ─── */
.book-management-container {
  min-height: 100vh;
  padding: 20px 0;
  font-size: 13px;
  color: var(--pb-color-text);
}

/* ─── 페이지 헤더 ─── */
.page-header {
  margin-bottom: 16px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.title-section { flex: 1; }

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0 0 3px;
}

.page-subtitle {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0;
}

.header-actions { display: flex; gap: 8px; }

/* ─── 버튼 공통 ─── */
.register-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  background: var(--pb-color-brand);
  color: #fff;
  border: none;
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  text-decoration: none;
  white-space: nowrap;
}
.register-btn:hover { background: var(--pb-color-brand-strong); }

/* ─── 필터 카드 ─── */
.filter-section { margin-bottom: 14px; }

.filter-card {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-xs);
}

.filter-content {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.primary-filters { flex: 1; }

.action-controls {
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--pb-color-border);
  padding-top: 10px;
}

.control-group { display: flex; gap: 8px; }

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 130px;
}

.search-group { min-width: 260px; }

.filter-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--pb-color-text-soft);
}

/* 검색 인풋 */
.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 10px;
  color: var(--pb-color-text-soft);
  pointer-events: none;
  z-index: 1;
}

.search-input {
  width: 100%;
  height: 34px;
  padding: 0 32px 0 34px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s, box-shadow 0.15s;
}
.search-input::placeholder { color: var(--pb-color-text-soft); }
.search-input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

.clear-search-btn {
  position: absolute;
  right: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: var(--pb-color-text-soft);
  cursor: pointer;
  border-radius: var(--pb-radius-xs);
  transition: background 0.15s, color 0.15s;
}
.clear-search-btn:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
}

/* 셀렉트 */
.filter-select {
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s, box-shadow 0.15s;
  min-width: 110px;
}
.filter-select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}
.filter-select:disabled {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-soft);
  opacity: 0.7;
}

.filter-input {
  height: 32px;
  padding: 0 10px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 12px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  width: 130px;
  transition: border-color 0.15s;
}
.filter-input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

/* 초기화 버튼 */
.reset-filters-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 12px;
  background: var(--pb-color-surface);
  color: var(--pb-color-danger);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.reset-filters-btn:hover {
  background: var(--pb-color-danger-soft);
  border-color: var(--pb-color-danger);
}

/* 프린트 컨트롤 */
.print-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.print-toggle { display: flex; align-items: center; }

.toggle-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.toggle-input { display: none; }

.toggle-slider {
  position: relative;
  width: 40px;
  height: 22px;
  background: var(--pb-color-border);
  border-radius: 22px;
  transition: background 0.2s;
  flex-shrink: 0;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  background: var(--pb-color-surface);
  border-radius: 50%;
  transition: transform 0.2s;
  box-shadow: var(--pb-shadow-xs);
}

.toggle-input:checked + .toggle-slider { background: var(--pb-color-brand); }
.toggle-input:checked + .toggle-slider::before { transform: translateX(18px); }

.toggle-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--pb-color-text);
  white-space: nowrap;
}

.batch-print-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  background: var(--pb-color-brand);
  color: #fff;
  border: none;
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.batch-print-btn:hover:not(:disabled) { background: var(--pb-color-brand-strong); }
.batch-print-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.count-badge {
  background: rgba(255,255,255,0.22);
  border: 1px solid rgba(255,255,255,0.3);
  padding: 1px 6px;
  border-radius: var(--pb-radius-xs);
  font-size: 12px;
  font-weight: 600;
}

/* ─── 통계 섹션 ─── */
.stats-section {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-xs);
  min-width: 140px;
  flex: 1;
}

.stat-card.total-books .stat-icon    { background: var(--pb-color-accent-soft);  color: var(--pb-color-accent); }
.stat-card.borrowed-books .stat-icon { background: var(--pb-color-danger-soft);  color: var(--pb-color-danger); }
.stat-card.available-books .stat-icon{ background: var(--pb-color-success-soft); color: var(--pb-color-success); }
.stat-card.unavailable-books .stat-icon{ background: var(--pb-color-warning-soft); color: var(--pb-color-warning); }
.stat-card.print-ready .stat-icon    { background: var(--pb-color-surface-muted); color: var(--pb-color-text-muted); }

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--pb-radius-md);
  flex-shrink: 0;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: var(--pb-color-heading);
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: var(--pb-color-text-muted);
  margin-top: 2px;
}

/* ─── 테이블 섹션 ─── */
.table-section { margin: 0; }

.table-card {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  overflow: hidden;
  box-shadow: var(--pb-shadow-xs);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-subtle);
}

.table-header h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.export-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 12px;
  background: #fff;
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text-primary);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.export-btn:hover { background: var(--pb-color-bg-subtle); }

.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 12px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text-muted);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.refresh-btn:hover:not(:disabled) {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
}
.refresh-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.refresh-btn .spinning { animation: spin 1s linear infinite; }

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.result-count {
  font-size: 12px;
  color: var(--pb-color-text-soft);
  white-space: nowrap;
}

.table-wrapper { overflow-x: auto; }

/* ─── 테이블 ─── */
.books-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  min-width: 1000px;
}

.books-table th {
  padding: 9px 6px;
  background: var(--pb-color-surface-muted);
  border-bottom: 1px solid var(--pb-color-border);
  color: var(--pb-color-text-soft);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  text-align: left;
  position: sticky;
  top: 0;
  z-index: 10;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.books-table td {
  padding: 7px 6px;
  border-bottom: 1px solid var(--pb-color-border);
  font-size: 13px;
  vertical-align: middle;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--pb-color-text);
}

/* 컬럼 너비 */
.col-checkbox { width: 38px; text-align: center; }
.col-title    { width: 178px; }
.col-isbn     { width: 84px; }
.col-author   { width: 78px; }
.col-publisher{ width: 78px; }
.col-date     { width: 68px; }
.col-category { width: 74px; }
.col-count    { width: 44px; }
.col-status   { width: 70px; }
.col-barcode  { width: 118px; }
.col-actions  { width: 64px; }

/* 행 상태 */
.book-row:hover td { background: var(--pb-color-surface-muted); }

.book-row.active-row td {
  background: var(--pb-color-surface-subtle);
}
.book-row.active-row td:first-child { border-left: 2px solid var(--pb-color-brand); }
.book-row.active-row:hover td { background: var(--pb-color-surface-muted); }

.book-row.selectable-row { cursor: pointer; }

.book-row.selected-row td {
  background: var(--pb-color-surface-subtle);
}
.book-row.selected-row td:first-child { border-left: 2px solid var(--pb-color-brand); }
.book-row.selected-row:hover td { background: var(--pb-color-surface-muted); }

/* 체크박스 */
.checkbox-input {
  width: 15px;
  height: 15px;
  cursor: pointer;
  accent-color: var(--pb-color-brand);
}
.checkbox-input:disabled { cursor: not-allowed; opacity: 0.35; }

/* 선택 정보 */
.print-selection-info {
  display: flex;
  align-items: center;
  padding: 3px 10px;
  background: var(--pb-color-surface-muted);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 12px;
  color: var(--pb-color-text-muted);
}

.selection-count { font-weight: 500; }
.selection-count strong { color: var(--pb-color-brand); font-weight: 700; }

/* 제목 셀 */
.book-title .title-text {
  font-weight: 500;
  color: var(--pb-color-heading);
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 인라인 셀렉트 */
.category-select {
  width: 100%;
  height: 26px;
  padding: 0 4px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-xs);
  font-size: 12px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s, box-shadow 0.15s;
}
.category-select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 2px var(--pb-color-brand-soft);
}
.category-select:disabled {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-soft);
  opacity: 0.65;
}

/* 번호 인풋 */
.count-input {
  width: 100%;
  height: 26px;
  padding: 0 4px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-xs);
  font-size: 12px;
  text-align: center;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s, box-shadow 0.15s;
}
.count-input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 2px var(--pb-color-brand-soft);
}

/* 상태 배지 */
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 99px;
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
  letter-spacing: 0.01em;
}
.status-borrowed  { background: var(--pb-color-danger-soft);  color: var(--pb-color-danger); }
.status-available { background: var(--pb-color-success-soft); color: var(--pb-color-success); }
.status-unavailable { background: var(--pb-color-surface-muted); color: var(--pb-color-text-muted); }

/* 바코드 인풋 */
.barcode-input {
  width: 100%;
  height: 26px;
  padding: 0 4px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-xs);
  font-size: 11px;
  background: var(--pb-color-surface-muted);
  font-family: 'Courier New', monospace;
  color: var(--pb-color-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 액션 버튼 */
.action-buttons { display: flex; gap: 3px; justify-content: center; }

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: 1px solid transparent;
  border-radius: var(--pb-radius-xs);
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

.action-btn svg { width: 11px; height: 11px; }

.barcode-btn {
  background: var(--pb-color-surface);
  border-color: var(--pb-color-border);
  color: var(--pb-color-text-muted);
}
.barcode-btn:hover {
  background: var(--pb-color-surface-muted);
  border-color: var(--pb-color-border-strong);
}

.delete-btn {
  background: var(--pb-color-danger-soft);
  border-color: rgba(217, 48, 37, 0.25);
  color: var(--pb-color-danger);
}
.delete-btn:hover {
  background: var(--pb-color-danger);
  border-color: var(--pb-color-danger);
  color: #fff;
}

/* ─── 빈 상태 ─── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 24px;
  color: var(--pb-color-text-soft);
  text-align: center;
}
.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.35;
  color: var(--pb-color-text-soft);
}
.empty-state h3 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px;
  color: var(--pb-color-heading);
}
.empty-state p { margin: 0; font-size: 13px; color: var(--pb-color-text-muted); }

/* ─── 페이지네이션 (GitLab Offset style) ─── */
.gl-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 4px;
  margin-top: 8px;
}
.gl-pagination-info {
  font-size: 13px;
  color: var(--pb-color-text-muted);
}
.gl-pagination-nav {
  display: flex;
  align-items: center;
  gap: 2px;
}
.gl-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  font-size: 13px;
  transition: background 0.12s, color 0.12s, border-color 0.12s;
  white-space: nowrap;
}
.gl-page-btn:hover:not(:disabled):not(.active) {
  background: var(--pb-color-surface-muted);
  border-color: var(--pb-color-border-strong);
}
.gl-page-btn.active {
  background: var(--pb-color-brand);
  color: #fff;
  border-color: var(--pb-color-brand);
  font-weight: 600;
}
.gl-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.gl-page-btn.prev-btn,
.gl-page-btn.next-btn { padding: 0 10px; }
.gl-page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 13px;
  color: var(--pb-color-text-soft);
  cursor: default;
  user-select: none;
}

/* ─── 반응형 ─── */
@media (max-width: 1400px) {
  .filter-row { flex-wrap: wrap; }
  .primary-filters { width: 100%; }
  .action-controls {
    width: 100%;
    padding-top: 10px;
    border-top: 1px solid var(--pb-color-border);
  }
}

@media (max-width: 1200px) {
  .header-content { flex-direction: column; align-items: stretch; gap: 10px; }
  .filter-row { flex-direction: column; align-items: stretch; }
  .filter-group { width: 100%; min-width: auto; }
  .search-group { min-width: auto; }
  .action-controls { flex-direction: column; gap: 10px; align-items: stretch; }
  .print-controls { justify-content: space-between; }
  .stats-section { flex-direction: column; }
}

@media (max-width: 768px) {
  .page-title { font-size: 16px; }
  .filter-content { padding: 12px; }
  .table-header { flex-direction: column; align-items: flex-start; gap: 8px; padding: 10px 14px; }
  .table-actions { width: 100%; justify-content: flex-end; }
  .books-table { min-width: 850px; }
  .col-title     { width: 138px; }
  .col-isbn      { width: 70px; }
  .col-author    { width: 64px; }
  .col-publisher { width: 64px; }
  .col-date      { width: 58px; }
  .col-category  { width: 58px; }
  .col-count     { width: 40px; }
  .col-status    { width: 60px; }
  .col-barcode   { width: 96px; }
  .col-actions   { width: 52px; }
  .stat-card { min-width: unset; }
}

@media (max-width: 480px) {
  .toggle-text { display: none; }
  .batch-print-btn { padding: 0 10px; font-size: 12px; }
  .result-count { font-size: 12px; }
}
</style>