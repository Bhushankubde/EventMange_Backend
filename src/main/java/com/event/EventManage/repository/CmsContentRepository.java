package com.event.EventManage.repository;

import com.event.EventManage.model.CmsContent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CmsContentRepository extends JpaRepository<CmsContent, String> {
    Optional<CmsContent> findByContentKey(String contentKey);
    List<CmsContent> findByCategory(String category);
}
