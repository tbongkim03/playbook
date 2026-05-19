package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import playbook.encore.back.config.BookReminderScheduler;
import playbook.encore.back.config.CourseEndReturnReminderScheduler;

@RestController
public class TestController {

    private final CourseEndReturnReminderScheduler courseEndReturnReminderScheduler;
    private final BookReminderScheduler bookReminderScheduler;

    @Autowired
    public TestController(CourseEndReturnReminderScheduler courseEndReturnReminderScheduler,
                          BookReminderScheduler bookReminderScheduler) {
        this.courseEndReturnReminderScheduler = courseEndReturnReminderScheduler;
        this.bookReminderScheduler = bookReminderScheduler;
    }

    // 수동 테스트: 과정 종료 상태 업데이트
    @GetMapping("/admin/test/update-finished-courses")
    public String testUpdateFinishedCourses() {
        bookReminderScheduler.updateStatusForFinishedCourses();
        return "과정 종료 상태 업데이트 테스트 실행됨";
    }

//    @GetMapping("/admin/test/course-reminder")
//    public String testCourseReminder() {
//        courseEndReturnReminderScheduler.testCourseEndReminder();
//        return "과정 종료 알림 테스트 실행됨";
//    }
//
//    @GetMapping("/admin/test/course-reminder/{days}")
//    public String testSpecificDayReminder(@PathVariable int days) {
//        courseEndReturnReminderScheduler.testSpecificDayReminder(days);
//        return days + "일 후 과정 종료 알림 테스트 실행됨";
//    }
}
