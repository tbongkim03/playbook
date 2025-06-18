package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.BookDAO;
import playbook.encore.back.data.dto.book.BookRequestDto;
import playbook.encore.back.data.dto.book.BookResponseDto;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.SortFirst;
import playbook.encore.back.data.entity.SortSecond;
import playbook.encore.back.data.repository.BookRepository;
import playbook.encore.back.data.repository.SortSecondRepository;
import playbook.encore.back.service.BookService;

import java.util.List;

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
        SortFirst sortFirst = sortSecond.getSeqSortFirst();

        Book book = new Book();
        book.setSeqSortSecond(sortSecond);
        book.setIsbnBook(bookRequestDto.getIsbnBook());
        book.setTitleBook(bookRequestDto.getTitleBook());
        book.setAuthorBook(bookRequestDto.getAuthorBook());
        book.setPublisherBook(bookRequestDto.getPublisherBook());
        book.setPublishDateBook(bookRequestDto.getPublishDateBook());

        // 바코드 문자열 생성 로직 구현, 책 갯수 파악 로직 구현
        book.setBarcodeBook(bookRequestDto.getBarcodeBook());
        book.setCntBook(bookRequestDto.getCntBook());

        Book savedBook = bookDAO.insertBook(book);
        BookResponseDto bookResponseDto = convertToDto(savedBook);

        return bookResponseDto;
    }

    @Override
    public List<BookResponseDto> getAllBook() throws Exception {
        List<Book> bookList = bookDAO.selectAllBook();

        List<BookResponseDto> responseList = new java.util.ArrayList<>();
        for (Book book : bookList) {
            BookResponseDto bookResponseDto = convertToDto(book);
            responseList.add(bookResponseDto);
        }

        return responseList;
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
        SortSecond sortSecond = sortSecondRepository.findById(bookRequestDto.getSeqSortSecond())
                .orElseThrow(() -> new IllegalArgumentException("해당 대분류가 존재하지 않습니다."));
        SortFirst sortFirst = sortSecond.getSeqSortFirst();

        Book updatedBook = bookDAO.updateBook(
                bookId,
                sortSecond.getSeqSortSecond(),
                bookRequestDto.getIsbnBook(),
                bookRequestDto.getTitleBook(),
                bookRequestDto.getAuthorBook(),
                bookRequestDto.getPublisherBook(),
                bookRequestDto.getPublishDateBook(),
                bookRequestDto.getBarcodeBook(),
                bookRequestDto.getCntBook()
        );
        BookResponseDto bookResponseDto = convertToDto(updatedBook);

        return bookResponseDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBookById(Integer bookId) throws Exception {
        Book selectedBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("삭제에 실패했습니다. 해당 도서는 존재하지 않습니다."));

        bookRepository.delete(selectedBook);
    }
}
