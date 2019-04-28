package xyz.nowiknowmy.hogwarts.domain;

import javax.persistence.*;

@Entity
@Table(name = "revisions")
public class Revision extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
