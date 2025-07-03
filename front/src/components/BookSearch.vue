<template>
    <form action="" class="search-form">
        <div class="input-group mb-3 inpg" id="inputArea">
            <input type="text" class="form-control" placeholder="도서명" aria-label="bookTitle" aria-describedby="basic-addon1">
            <span class="icon">&#x1F50D;</span>
    
            <ul class="autocomplete-list">
                <li>도서명 추천 1</li>
                <li>도서명 추천 2</li>
                <li>도서명 추천 3</li>
            </ul>
        </div>
        <button type="submit" class="btn btn-primary">검색</button>
    </form>
</template>

<script>
export default {
    name: 'BookSearch'
}
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
    padding: 10px 40px 10px 10px;
    border: 1px solid #ccc;
    border-radius: 10px;
    border-bottom: none;
    box-sizing: border-box;
    z-index: 0;
    position: relative;
    background-color: white;
}

#inputArea input:focus {
    border-bottom: none;
}

#inputArea .icon {
    position: absolute;
    top: 50%;
    right: 10px;
    transform: translateY(-50%);
    font-size: 18px;
    color: #555;
    pointer-events: none;
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