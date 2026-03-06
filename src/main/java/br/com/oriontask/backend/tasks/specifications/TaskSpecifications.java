package br.com.oriontask.backend.tasks.specifications;

import br.com.oriontask.backend.shared.enums.TaskStatus;
import br.com.oriontask.backend.tasks.model.Tasks;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecifications {

  public static Specification<Tasks> byUser(UUID userId) {
    return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
  }

  public static Specification<Tasks> byDharma(Long dharmasId) {
    return (root, query, cb) -> cb.equal(root.get("dharmas").get("id"), dharmasId);
  }

  public static Specification<Tasks> byStatus(TaskStatus status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  public static Specification<Tasks> includeHidden(Boolean includeHidden) {
    return (root, query, cb) -> {
      if (Boolean.TRUE.equals(includeHidden)) {
        return cb.conjunction();
      }
      return cb.isFalse(root.get("hidden"));
    };
  }
}
