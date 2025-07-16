package playbook.encore.back.data.entity;

import jakarta.persistence.*;
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

    @Column(name = "name_course", nullable = false)
    private String nameCourse;

    @Column(name = "start_dt_course", nullable = false)
    private LocalDate startDtCourse;

    @Column(name = "finish_dt_course", nullable = false)
    private LocalDate finistDtCourse;
}
