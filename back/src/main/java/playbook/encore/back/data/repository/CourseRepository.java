package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import playbook.encore.back.data.entity.Course;


public interface CourseRepository extends JpaRepository<Course, Integer> {
    
}
