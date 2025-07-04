package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.BookDAO;
import playbook.encore.back.data.dto.book.BookCountResponseDto;
import playbook.encore.back.data.dto.book.BookListResponseDto;
import playbook.encore.back.data.dto.book.BookRequestDto;
import playbook.encore.back.data.dto.book.BookResponseDto;
import playbook.encore.back.data.dto.book.BookSearchResponseDto;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.SortSecond;
import playbook.encore.back.data.repository.BookRepository;
import playbook.encore.back.data.repository.SortSecondRepository;
import playbook.encore.back.service.BookService;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookDAO bookDAO;
    private final BookRepository bookRepository;
    private final SortSecondRepository sortSecondRepository;

    @Autowired
    public BookServiceImpl(BookDAO bookDAO, BookRepository bookRepository, SortSecondRepository sortSecondRepository) {
        this.bookDAO = bookDAO;
        this.bookRepository = bookRepository;
        this.sortSecondRepository = sortSecondRepository;
    }

    private BookResponseDto convertToDto(Book entity) {
        return new BookResponseDto(
                entity.getSeqBook(),
                entity.getSeqSortSecond().getSeqSortSecond(),
                entity.getIsbnBook(),
                entity.getTitleBook(),
                entity.getAuthorBook(),
                entity.getPublisherBook(),
                entity.getPublishDateBook(),
                entity.getBarcodeBook(),
                entity.getCntBook()
        );
    }

    @Override
    @Transactional
    public BookResponseDto insertBook(BookRequestDto bookRequestDto) {
        SortSecond sortSecond = sortSecondRepository.findById(bookRequestDto.getSeqSortSecond())
                .orElseThrow(() -> new IllegalArgumentException("해당 분류가 존재하지 않습니다"));
        // SortFirst sortFirst = sortSecond.getSeqSortFirst();

        Book book = Book.builder()
                .seqSortSecond(sortSecond)
                .isbnBook(bookRequestDto.getIsbnBook())
                .titleBook(bookRequestDto.getTitleBook())
                .authorBook(bookRequestDto.getAuthorBook())
                .publisherBook(bookRequestDto.getPublisherBook())
                .publishDateBook(bookRequestDto.getPublishDateBook())
                .barcodeBook(null)
                .cntBook(null)
                .build();

        // 바코드 문자열 생성 로직 구현, 책 갯수 파악 로직 구현 deprecated

        Book savedBook = bookDAO.insertBook(book);
        BookResponseDto bookResponseDto = convertToDto(savedBook);

        return bookResponseDto;
    }

    @Override
    public BookListResponseDto getBookList(int page) throws Exception {
        Page<Book> bookPage = bookDAO.selectBookListByPage(page);

        List<BookResponseDto> content = bookPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int totalCount = (int) bookPage.getTotalElements();
        BookListResponseDto bookListResponseDto = new BookListResponseDto(content, totalCount);
        return bookListResponseDto;
    }


    @Override
    public BookResponseDto getBookById(Integer bookId) throws Exception {
        Book selectedBook = bookDAO.selectBookById(bookId);
        BookResponseDto bookResponseDto = convertToDto(selectedBook);

        return bookResponseDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookResponseDto changeBook(Integer bookId, BookRequestDto bookRequestDto) throws Exception {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("해당 책이 존재하지 않습니다."));

        SortSecond sortSecond = sortSecondRepository.findById(bookRequestDto.getSeqSortSecond())
                .orElseThrow(() -> new IllegalArgumentException("해당 중분류가 존재하지 않습니다."));

        sortSecond.setSeqSortSecond(bookRequestDto.getSeqSortSecond());
        Book updatedBook = Book.builder()
                .seqBook(bookId)
                .seqSortSecond(sortSecond)
                .isbnBook(existingBook.getIsbnBook())
                .titleBook(existingBook.getTitleBook())
                .authorBook(existingBook.getAuthorBook())
                .publisherBook(existingBook.getPublisherBook())
                .publishDateBook(existingBook.getPublishDateBook())
                .barcodeBook(existingBook.getBarcodeBook())
                .cntBook(existingBook.getCntBook())
                .build();

        Book changedBook = bookDAO.updateBook(updatedBook);

        BookResponseDto bookResponseDto = convertToDto(changedBook);

        return bookResponseDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBookById(Integer bookId) throws Exception {
        Book selectedBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("삭제에 실패했습니다. 해당 도서는 존재하지 않습니다."));

        bookDAO.deleteBook(selectedBook);
    }

    @Override
    @Transactional
    public BookCountResponseDto getBookCount(String isbn) throws Exception {
        Integer counts = bookRepository.countByIsbnBook(isbn);
        return new BookCountResponseDto(isbn, counts);
    }

    @Override
    public List<BookSearchResponseDto> searchBookTitles(String titleBook) throws Exception {
        List<Book> bookList = bookDAO.searchBooksRelated(titleBook);

        List<BookSearchResponseDto> responseList = new java.util.ArrayList<>();
        for (Book book : bookList) {
            BookSearchResponseDto bookSearchResponseDto = new BookSearchResponseDto(book.getTitleBook());
            responseList.add(bookSearchResponseDto);
        }

        return responseList;
    }

    @Override
    public BookListResponseDto searchBooksByTitle(String titleBook) throws Exception {
        List<Book> books = bookDAO.searchBooksResult(titleBook);

        List<BookResponseDto> content = books.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int totalCount = content.size();
        return new BookListResponseDto(content, totalCount);
    }
}
