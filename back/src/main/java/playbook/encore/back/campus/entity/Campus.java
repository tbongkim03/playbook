package playbook.encore.back.campus.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Campus 엔티티
 * 캠퍼스 정보를 관리하는 엔티티
 * - 서초, G밸리, 동작 캠퍼스 등
 * - Course, Book, Admin, History와 연관관계
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_campus")
public class Campus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_campus", nullable = false)
    private Integer seqCampus;

    @Column(name = "name_campus", nullable = false, unique = true)
    private String nameCampus;

    @Column(name = "location_campus")
    private String locationCampus;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
