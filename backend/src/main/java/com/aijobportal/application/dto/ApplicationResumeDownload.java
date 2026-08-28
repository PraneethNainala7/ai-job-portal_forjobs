package com.aijobportal.application.dto;

import java.nio.file.Path;

public record ApplicationResumeDownload(
        Path path,
        String fileName,
        String mediaType
) {
}
