package com.example.ecommerce.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.config.MediaProperties;
import com.example.ecommerce.dto.MediaUploadResponse;
import com.example.ecommerce.exception.InvalidFileException;
import com.example.ecommerce.exception.StorageException;

import jakarta.annotation.PostConstruct;

/**
 * Phase 4, Module 4: local-disk media storage.
 *
 * <p>Validation runs in four layers, because any single one can be defeated:
 * declared MIME type, file extension, byte length, and finally an actual decode
 * attempt. The decode is what stops a renamed {@code .exe} that arrives with a
 * forged {@code image/png} content type, since a non-image simply fails to
 * decode.
 *
 * <p>Client-supplied filenames are never used on disk. Every upload is written
 * under a freshly generated name, which removes path traversal
 * ({@code ../../etc/passwd}) as a possibility rather than trying to filter it.
 */
@Service
public class MediaStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/jpg", "image/png");

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png");

    /**
     * Sub-folders callers may write into. A whitelist rather than free text, so
     * a request cannot create arbitrary directories.
     */
    private static final Set<String> ALLOWED_CATEGORIES =
            Set.of("products", "vendors", "reviews");

    private static final SecureRandom RANDOM = new SecureRandom();

    private final MediaProperties mediaProperties;
    private final AuditService auditService;

    private Path baseDirectory;

    public MediaStorageService(
            MediaProperties mediaProperties,
            AuditService auditService) {

        this.mediaProperties = mediaProperties;
        this.auditService = auditService;
    }

    /**
     * Creates the upload directories at startup so the first request does not
     * fail on a missing folder.
     */
    @PostConstruct
    public void initialiseStorage() {

        this.baseDirectory =
                Paths.get(mediaProperties.getUploadDir())
                        .toAbsolutePath()
                        .normalize();

        try {

            Files.createDirectories(baseDirectory);

            for (String category : ALLOWED_CATEGORIES) {
                Files.createDirectories(baseDirectory.resolve(category));
            }

        } catch (IOException ex) {

            throw new StorageException(
                    "Could not initialise the media upload directory at "
                            + baseDirectory,
                    ex
            );
        }
    }

    /**
     * Validates and stores one uploaded image.
     *
     * @param file     the multipart payload
     * @param category one of {@code products}, {@code vendors}, {@code reviews}
     * @return the stored filename plus the public URL it is served under
     * @throws InvalidFileException when the payload is empty, too large, not a
     *                              JPEG or PNG, or not a decodable image
     */
    public MediaUploadResponse store(
            MultipartFile file,
            String category) {

        // The file itself is checked first: a caller who forgot the file part
        // should hear about that before hearing about the category.
        if (file == null || file.isEmpty()) {

            throw new InvalidFileException(
                    "A file is required. Send it as multipart/form-data "
                            + "under the field name 'file'."
            );
        }

        String targetCategory =
                resolveCategory(category);

        long maxBytes =
                mediaProperties.getMaxFileSizeBytes();

        if (file.getSize() > maxBytes) {

            throw new InvalidFileException(
                    String.format(
                            "File is too large: %s. The maximum allowed size "
                                    + "is %s.",
                            humanReadable(file.getSize()),
                            humanReadable(maxBytes)
                    )
            );
        }

        String contentType =
                normaliseContentType(file.getContentType());

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new InvalidFileException(
                    String.format(
                            "Unsupported content type '%s'. Only JPEG and PNG "
                                    + "images are accepted (image/jpeg, "
                                    + "image/png).",
                            file.getContentType() == null
                                    ? "unknown"
                                    : file.getContentType()
                    )
            );
        }

        String extension =
                extractExtension(file.getOriginalFilename());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {

            throw new InvalidFileException(
                    String.format(
                            "Unsupported file extension '.%s'. Only .jpg, "
                                    + ".jpeg and .png files are accepted.",
                            extension.isEmpty() ? "" : extension
                    )
            );
        }

        byte[] bytes = readBytes(file);

        // Fourth and decisive check: a spoofed content type cannot survive an
        // actual decode attempt.
        if (!isDecodableImage(bytes)) {

            throw new InvalidFileException(
                    "The uploaded file is not a valid image. Its contents "
                            + "could not be decoded as JPEG or PNG, even "
                            + "though it was declared as "
                            + contentType
                            + "."
            );
        }

        String storedFileName =
                generateFileName(extension);

        Path targetPath =
                baseDirectory
                        .resolve(targetCategory)
                        .resolve(storedFileName)
                        .normalize();

        // Defensive: the generated name contains no separators, so this cannot
        // currently fail. It is kept so a future change to the naming scheme
        // cannot silently reintroduce a traversal.
        if (!targetPath.startsWith(baseDirectory)) {

            throw new StorageException(
                    "Resolved upload path escapes the media directory"
            );
        }

        try {

            Files.write(
                    targetPath,
                    bytes,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );

        } catch (IOException ex) {

            throw new StorageException(
                    "Failed to store uploaded file " + storedFileName,
                    ex
            );
        }

        String publicUrl =
                buildPublicUrl(targetCategory, storedFileName);

        Map<String, Object> details =
                new HashMap<>();

        details.put("fileName", storedFileName);
        details.put("category", targetCategory);
        details.put("url", publicUrl);
        details.put("contentType", contentType);
        details.put("sizeBytes", (long) bytes.length);

        auditService.log(
                "Media",
                "MEDIA_UPLOADED",
                details
        );

        return new MediaUploadResponse(
                storedFileName,
                publicUrl,
                contentType,
                bytes.length
        );
    }

    /**
     * Absolute directory the static resource handler serves from.
     */
    public Path getBaseDirectory() {

        if (baseDirectory == null) {

            return Paths.get(mediaProperties.getUploadDir())
                    .toAbsolutePath()
                    .normalize();
        }

        return baseDirectory;
    }

    private String resolveCategory(String category) {

        if (category == null || category.isBlank()) {
            return "products";
        }

        String normalised =
                category.trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED_CATEGORIES.contains(normalised)) {

            throw new InvalidFileException(
                    "Unsupported category '"
                            + category
                            + "'. Allowed values are: products, vendors, "
                            + "reviews."
            );
        }

        return normalised;
    }

    private byte[] readBytes(MultipartFile file) {

        try {
            return file.getBytes();

        } catch (IOException ex) {

            throw new StorageException(
                    "Could not read the uploaded file stream",
                    ex
            );
        }
    }

    private boolean isDecodableImage(byte[] bytes) {

        try (ByteArrayInputStream input =
                     new ByteArrayInputStream(bytes)) {

            return ImageIO.read(input) != null;

        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Produces names in the shape {@code img_<epochMillis>_<random>.png},
     * matching the {@code img_1024.png} example in the specification while
     * staying collision-free under concurrent uploads.
     */
    private String generateFileName(String extension) {

        String suffix =
                Long.toHexString(
                        RANDOM.nextLong() & 0xFFFFFFFFL
                );

        return "img_"
                + Instant.now().toEpochMilli()
                + "_"
                + suffix
                + "."
                + extension;
    }

    private String buildPublicUrl(
            String category,
            String fileName) {

        String basePath =
                mediaProperties.getPublicBasePath();

        if (basePath == null || basePath.isBlank()) {
            basePath = "/uploads";
        }

        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }

        if (basePath.endsWith("/")) {
            basePath =
                    basePath.substring(0, basePath.length() - 1);
        }

        return basePath + "/" + category + "/" + fileName;
    }

    private String normaliseContentType(String contentType) {

        if (contentType == null) {
            return "";
        }

        int separator = contentType.indexOf(';');

        String withoutParameters =
                separator >= 0
                        ? contentType.substring(0, separator)
                        : contentType;

        return withoutParameters
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String extractExtension(String originalFilename) {

        if (originalFilename == null) {
            return "";
        }

        // Strip any directory component a client may have sent before looking
        // for the extension.
        String bareName =
                Paths.get(originalFilename)
                        .getFileName()
                        .toString();

        int dot = bareName.lastIndexOf('.');

        if (dot < 0 || dot == bareName.length() - 1) {
            return "";
        }

        return bareName
                .substring(dot + 1)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String humanReadable(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024 * 1024) {
            return String.format(
                    Locale.US,
                    "%.1f KB",
                    bytes / 1024.0
            );
        }

        return String.format(
                Locale.US,
                "%.2f MB",
                bytes / (1024.0 * 1024.0)
        );
    }
}
