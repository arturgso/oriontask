package br.com.oriontask.backend.model;

import java.sql.Timestamp;

import org.hibernate.validator.constraints.Length;

import br.com.oriontask.backend.enums.EffortLevel;
import br.com.oriontask.backend.enums.KarmaType;
import br.com.oriontask.backend.enums.TaskStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tab_tasks")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Tasks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dharma_id", nullable = false)
    private Dharmas dharmas;

    @Size(min = 5, max = 60, message = "The title must be between 5 and 60 characters")
    private String title;

    @Length(max = 200, message = "The description must be at most 200 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    private KarmaType karmaType;

    @Enumerated(EnumType.STRING)
    private EffortLevel effortLevel;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Builder.Default
    private Boolean hidden = false;

    @Builder.Default
    private Timestamp completedAt = null;

    private Timestamp snoozedUntil;

    @Builder.Default
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Builder.Default
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
