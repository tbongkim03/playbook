package playbook.encore.back.course.entity;

import jakarta.persistence.*;
import playbook.encore.back.campus.entity.Campus;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_course", nullable = false)
    private Integer seqCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_campus", nullable = true)
    private Campus seqCampus;

    @Column(name = "name_course", nullable = false)
    private String nameCourse;

    @Column(name = "start_dt_course", nullable = false)
    private LocalDate startDtCourse;

    @Column(name = "finish_dt_course", nullable = false)
    private LocalDate finishDtCourse;
}
