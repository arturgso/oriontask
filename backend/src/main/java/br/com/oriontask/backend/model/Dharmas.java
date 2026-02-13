package br.com.oriontask.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tab_dharma")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Dharmas {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private Users user;

  @Length(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
  private String name;

  @Pattern(
      regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
      message = "Color must be hexadecimal, e.g., #FFFFFF")
  private String color;

  @Builder.Default private Boolean hidden = false;

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;
}
