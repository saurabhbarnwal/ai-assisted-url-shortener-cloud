package com.company.urlshortener.repository;

import com.company.urlshortener.entity.AnalyticsEntity;
import com.company.urlshortener.entity.UrlEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for click analytics persistence operations. */
public interface AnalyticsRepository extends JpaRepository<AnalyticsEntity, Long> {

  /** Counts analytics events for a URL. */
  long countByUrl(UrlEntity url);

  /** Finds recent analytics events for a URL. */
  List<AnalyticsEntity> findByUrlOrderByClickTimestampDesc(UrlEntity url, Pageable pageable);
}
