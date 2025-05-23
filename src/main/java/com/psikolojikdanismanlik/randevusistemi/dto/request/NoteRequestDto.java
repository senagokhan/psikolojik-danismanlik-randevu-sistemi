package com.psikolojikdanismanlik.randevusistemi.dto.request;

public class NoteRequestDto {
    private String content;
    private boolean isPrivate;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
