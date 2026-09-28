package com.example.leavemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "leave_attachments")
public class Attachment {
    @Id private UUID id;
    @Column(nullable = false) private String originalFilename;
    @Column(nullable = false, length = 100) private String contentType;
    @Column(nullable = false) private long size;
    @Lob @Column(nullable = false) private byte[] content;

    protected Attachment() { }
    public Attachment(String filename, String contentType, byte[] content) {
        this.id = UUID.randomUUID(); this.originalFilename = filename; this.contentType = contentType;
        this.size = content.length; this.content = content.clone();
    }
    public UUID getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public byte[] getContent() { return content.clone(); }
}