<template>
    <form action="" class="search-form" @submit.prevent="onSubmit" ref="searchForm">
    <div class="input-group mb-3 inpg" id="inputArea">
      <input
        type="text"
        class="form-control"
        placeholder="도서명"
        aria-label="bookTitle"
        aria-describedby="basic-addon1"
        v-model="query"
        @input="onInput"
        @focus="onFocus"
        @blur="onBlur"
        autocomplete="off"
      />
      <span class="icon" @click="submitForm">&#x1F50D;</span>

      <ul class="autocomplete-list" v-if="isFocused && suggestions.length">
        <li
          v-for="(item, index) in suggestions"
          :key="index"
          @mousedown.prevent="selectSuggestion(item)"
        >
          {{ item }}
        </li>
      </ul>
    </div>
  </form>
</template>

<script>
export default {
  name: 'BookSearch',
  data() {
    return {
      query: '',
      suggestions: [],
      isFocused: false,
    };
  },
  methods: {
    submitForm() {
      // form 요소에 직접 submit() 호출
      this.$refs.searchForm.submit();
    },
    async fetchSuggestions() {
      if (!this.query.trim()) {
        this.suggestions = [];
        return;
      }
      try {
        const response = await fetch(
          `http://localhost:8080/books/search?q=${encodeURIComponent(this.query)}`
        );
        if (!response.ok) throw new Error('네트워크 오류');
        const data = await response.json();

        this.suggestions = data.map((item) => item.titleBook);
      } catch (error) {
        console.error('자동완성 요청 실패:', error);
        this.suggestions = [];
      }
    },
    selectSuggestion(suggestion) {
      this.query = suggestion;
      this.suggestions = [];
    },
    onInput() {
      this.fetchSuggestions();
    },
    onFocus() {
      this.isFocused = true;
      this.fetchSuggestions();
    },
    onBlur() {
      setTimeout(() => {
        this.isFocused = false;
        this.suggestions = [];
      }, 200);
    },
    onSubmit() {
      // 실제 검색 동작 처리, 예: API 호출, 라우팅 등
      console.log('검색 제출:', this.query);
      // 여기서 실제 검색 처리 코드를 작성
    },
  },
};
</script>

<style>
.search-form {
    display: grid;
    grid-template-columns: 9fr 1fr;
    column-gap: 10px;
    width: 100%;
}

#inputArea {
    position: relative;
    display: flex;
    width: 100%;
    height: 95%;
}

#inputArea input {
    width: 100%;
    padding: 10px 40px 10px 20px;
    border: 1px solid #ccc;
    border-radius: 10px;
    box-sizing: border-box;
    z-index: 0;
    position: relative;
    background-color: white;
    font-size: 18px;
}

#inputArea input:focus {
    border-bottom: none;
}

#inputArea .icon {
  cursor: pointer; /* 아이콘에 마우스 올리면 클릭 가능 느낌 */
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  font-size: 18px;
  color: #555;
  pointer-events: auto; /* 클릭 이벤트 활성화 */
}

.autocomplete-list {
    position: absolute;
    top: calc(100% - 7px);
    left: 1px;
    right: 0;
    margin: 0;
    padding: 0;
    list-style: none;
    border: 1px solid #ccc;
    border-top: none;
    border-top-left-radius: 0;
    border-top-right-radius: 0;
    border-bottom-left-radius: 10px;
    border-bottom-right-radius: 10px;
    background-color: white;
    max-height: 200px;
    overflow-y: auto;
    z-index: 1;
    box-sizing: border-box;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.autocomplete-list li {
    padding: 10px;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

.autocomplete-list li:hover {
    background-color: #f0f0f0;
}
</style>