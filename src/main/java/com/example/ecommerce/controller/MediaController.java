package com.example.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.dto.MediaUploadResponse;
import com.example.ecommerce.service.MediaStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Phase 4, Module 4: image upload.
 *
 * <p>Returns the public static path the file is served under, ready to be
 * stored on a product, a vendor logo, or a review.
 */
@RestController
@RequestMapping("/api/v1/media")
@Tag(
        name = "Media",
        description = "Image uploads for products, vendor logos and reviews"
)
public class MediaController {

    private final MediaStorageService mediaStorageService;

    public MediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    /**
     * Accepts a single JPEG or PNG of at most 2 MB.
     *
     * <p>Anything else, an .exe or .pdf, an oversized file, or a non-image with
     * a forged content type, is rejected with 400 and an explanatory message.
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload an image",
            description = "multipart/form-data with a 'file' part. Accepts "
                    + "image/jpeg and image/png up to 2 MB. Returns the public "
                    + "URL, for example /uploads/products/img_1024.png. "
                    + "Unsupported types or oversized files return 400."
    )
    public MediaUploadResponse upload(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam(
                    name = "category",
                    defaultValue = "products"
            )
            String category) {

        return mediaStorageService.store(file, category);
    }
}
