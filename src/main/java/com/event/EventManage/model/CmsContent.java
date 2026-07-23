package com.event.EventManage.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cms_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsContent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String contentKey;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String contentHtml;

    private String category; // FAQ, TERMS, PRIVACY, BANNERS
}
