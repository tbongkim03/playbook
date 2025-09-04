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
                @keydown="handleKeyDown"
                autocomplete="off" 
                ref="searchInput"
            />
            <span class="icon" @click="onSubmit">&#x1F50D;</span>
    
            <ul class="autocomplete-list" v-if="isFocused && suggestions.length">
                <li 
                    v-for="(item, index) in suggestions" 
                    :key="`suggestion-${index}`" 
                    :class="{ 
                        'active': index === selectedIndex,
                        'keyboard-selected': index === selectedIndex 
                    }"
                    @mousedown.prevent="selectSuggestion(item)"
                    @mouseenter="onMouseEnter(index)"
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
    emits: ['search'],
    data() {
        return {
            query: '',
            suggestions: [],
            isFocused: false,
            selectedIndex: -1, // 선택된 항목의 인덱스
        };
    },
    methods: {
        async fetchSuggestions() {
            if (!this.query.trim()) {
                this.suggestions = [];
                this.selectedIndex = -1;
                return;
            }
            try {
                const response = await fetch(
                    `http://localhost:8080/books/related?q=${encodeURIComponent(this.query)}`
                );
                if (!response.ok) throw new Error('네트워크 오류');
                const data = await response.json();

                // printCheckBook이 true인 항목들만 필터링
                const availableBooks = data.filter(item => item.printCheckBook === true);

                // 중복 제거
                const uniqueTitles = Array.from(new Set(availableBooks.map(item => item.titleBook)));

                this.suggestions = uniqueTitles;
                this.selectedIndex = -1; // 새로운 검색 결과가 나올 때 선택 초기화
            } catch (error) {
                alert('자동완성 요청 실패:', error);
                this.suggestions = [];
                this.selectedIndex = -1;
            }
        },
        selectSuggestion(suggestion) {
            this.query = suggestion;
            this.suggestions = [];
            this.selectedIndex = -1;
            this.isFocused = false;
            // 포커스 해제
            this.$refs.searchInput.blur();
            this.$emit('search', { query: suggestion, exact: true });
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
                this.selectedIndex = -1;
            }, 200);
        },
        handleKeyDown(event) {
            // 기존 JavaScript 입력 차단 로직 먼저 실행
            this.blockJavascriptInput(event);
            
            // 자동완성 목록이 없으면 키보드 네비게이션 스킵
            if (!this.suggestions.length) return;

            switch (event.key) {
                case 'ArrowDown':
                    event.preventDefault();
                    event.stopPropagation();
                    this.selectedIndex = Math.min(this.selectedIndex + 1, this.suggestions.length - 1);
                    this.scrollToSelected();
                    console.log('ArrowDown - selectedIndex:', this.selectedIndex);
                    break;
                case 'ArrowUp':
                    event.preventDefault();
                    event.stopPropagation();
                    this.selectedIndex = Math.max(this.selectedIndex - 1, -1);
                    this.scrollToSelected();
                    console.log('ArrowUp - selectedIndex:', this.selectedIndex);
                    break;
                case 'Enter':
                    if (this.selectedIndex >= 0) {
                        event.preventDefault();
                        event.stopPropagation();
                        // 선택된 항목이 있으면 해당 항목을 선택
                        this.selectSuggestion(this.suggestions[this.selectedIndex]);
                        console.log('Enter - selected item:', this.suggestions[this.selectedIndex]);
                    }
                    // selectedIndex가 -1이면 기본 폼 제출 동작 허용
                    break;
                case 'Escape':
                    event.preventDefault();
                    event.stopPropagation();
                    this.suggestions = [];
                    this.selectedIndex = -1;
                    this.isFocused = false;
                    this.$refs.searchInput.blur();
                    break;
            }
        },
        onMouseEnter(index) {
            this.selectedIndex = index;
            console.log('Mouse enter - selectedIndex:', this.selectedIndex);
        },
        scrollToSelected() {
            if (this.selectedIndex >= 0) {
                this.$nextTick(() => {
                    const listElement = document.querySelector('.autocomplete-list');
                    const selectedElement = listElement?.children[this.selectedIndex];
                    if (selectedElement) {
                        selectedElement.scrollIntoView({
                            block: 'nearest',
                            behavior: 'smooth'
                        });
                    }
                });
            }
        },
        async onSubmit() {
            console.log('검색어 제출:', this.query);
            this.suggestions = [];
            this.selectedIndex = -1;
            this.isFocused = false;
            // 검색 후 포커스 해제
            this.$refs.searchInput.blur();
            this.$emit('search', { query: this.query, exact: false });
        },
        blockJavascriptInput(event) {
            // 방향키와 Enter, Escape는 차단하지 않도록 수정
            const allowedKeys = ['ArrowDown', 'ArrowUp', 'Enter', 'Escape', 'Backspace', 'Delete'];
            if (allowedKeys.includes(event.key)) {
                return; // 허용된 키는 차단하지 않음
            }
        },
    },
};
</script>

<style>
.search-form {
    display: grid;
    column-gap: 10px;
    width: 70%;
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
    outline: none;
}

#inputArea .icon {
    cursor: pointer;
    position: absolute;
    top: 50%;
    right: 10px;
    transform: translateY(-50%);
    font-size: 18px;
    color: #555;
    pointer-events: auto;
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

.autocomplete-list li.active,
.autocomplete-list li.keyboard-selected {
    background-color: #e3f2fd !important;
    color: #1976d2 !important;
    font-weight: 500;
}

.autocomplete-list li:hover:not(.keyboard-selected) {
    background-color: #f5f5f5;
}
</style>