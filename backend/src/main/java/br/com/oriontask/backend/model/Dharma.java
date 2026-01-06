package br.com.oriontask.backend.model;

import java.sql.Timestamp;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tab_dharma")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Dharma {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Length(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
    private String name;

    @Length(max = 100, message = "Description must be at most 100 characters")
    private String description;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be hexadecimal, e.g., #FFFFFF")
    private String color;

    @Builder.Default
    private Boolean hidden = false;

    @Builder.Default
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Builder.Default
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
