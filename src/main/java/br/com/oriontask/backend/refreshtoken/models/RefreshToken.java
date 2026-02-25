package br.com.oriontask.backend.refreshtoken.models;

import br.com.oriontask.backend.users.model.Users;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tab_refresh_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(cascade = CascadeType.ALL)
  private Users user;

  private String tokenHash;
  private Timestamp expirationAt;

  @CreationTimestamp private Timestamp createdAt;
  @UpdateTimestamp private Timestamp updatedAt;
}
