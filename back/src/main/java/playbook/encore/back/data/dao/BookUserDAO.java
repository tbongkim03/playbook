package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.BookUser;

public interface BookUserDAO {

    BookUser createUser(BookUser bookUser);

    BookUser searchBookUserResultExact(String idUser);
    
}
