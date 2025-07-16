package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.service.BookUserService;

@RestController
@RequestMapping("/users")
public class BookUserController {
    
    private final BookUserService bookUserService;
    private final BookUserDAO bookUserDAO;

    @Autowired
    public BookUserController(BookUserService bookUserService, BookUserDAO bookUserDAO) {
        this.bookUserService = bookUserService;
        this.bookUserDAO = bookUserDAO;
    }

    // 회원가입 관련 부분
    
    // 로그인 관련 부분

    // 회원정보 관련 부분

}
