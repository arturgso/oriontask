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

    @Length(min = 3, max = 20, message = "O nome deve ter entre 3 e 20 caracteres")
    private String name;

    @Length(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    private String description;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "A cor deve estar no formato hexadecimal, por exemplo, #FFFFFF")
    private String color;

    @Builder.Default
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Builder.Default
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
