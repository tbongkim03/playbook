package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import playbook.encore.back.config.CourseEndReturnReminderScheduler;

@RestController
public class TestController {

    private final CourseEndReturnReminderScheduler courseEndReturnReminderScheduler;

    @Autowired
    public TestController(CourseEndReturnReminderScheduler courseEndReturnReminderScheduler) {
        this.courseEndReturnReminderScheduler = courseEndReturnReminderScheduler;
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
