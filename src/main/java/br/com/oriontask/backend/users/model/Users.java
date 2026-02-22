package br.com.oriontask.backend.users.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tab_users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Users {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isConfirmed = false;

  private String confirmationToken;

  private Timestamp confirmationTokenExpiresAt;

  @Builder.Default @CreationTimestamp
  private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

  @Builder.Default @UpdateTimestamp
  private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
