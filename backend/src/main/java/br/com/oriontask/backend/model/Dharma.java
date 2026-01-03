package br.com.oriontask.backend.model;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    private String name;
    private String description;
    private String color;

    @Builder.Default
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Builder.Default
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
