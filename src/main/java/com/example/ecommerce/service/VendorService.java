package com.example.ecommerce.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.VendorRequest;
import com.example.ecommerce.dto.VendorResponse;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.Vendor;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.repository.VendorRepository;

/**
 * Phase 4, Module 2: marketplace vendor administration.
 *
 * <p>Only ROLE_ADMIN reaches the mutating methods here (enforced by
 * {@code @PreAuthorize} on the controller). Creating a vendor may optionally
 * promote an existing account to ROLE_VENDOR and bind it to the new vendor,
 * which is how a seller gets the ability to manage its own catalogue.
 */
@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final AuditService auditService;

    public VendorService(
            VendorRepository vendorRepository,
            UserRepository userRepository,
            ReviewRepository reviewRepository,
            AuditService auditService) {

        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.auditService = auditService;
    }

    @Transactional
    public VendorResponse create(VendorRequest request) {

        String businessName =
                request.getBusinessName().trim();

        if (vendorRepository.existsByBusinessName(businessName)) {

            throw new IllegalArgumentException(
                    "A vendor with business name '"
                            + businessName
                            + "' already exists"
            );
        }

        Vendor vendor = new Vendor();

        vendor.setBusinessName(businessName);
        vendor.setSupportEmail(request.getSupportEmail().trim());
        vendor.setLogoUrl(normalise(request.getLogoUrl()));
        vendor.setRatingAverage(0.0);
        vendor.setIsVerified(false);

        Vendor savedVendor =
                vendorRepository.save(vendor);

        String ownerEmail =
                linkOwnerIfRequested(
                        savedVendor,
                        request.getOwnerEmail()
                );

        Map<String, Object> details =
                new HashMap<>();

        details.put("vendorId", savedVendor.getId());
        details.put("businessName", savedVendor.getBusinessName());
        details.put("supportEmail", savedVendor.getSupportEmail());
        details.put("ownerEmail", ownerEmail);

        auditService.log(
                "Vendor",
                "VENDOR_CREATED",
                details
        );

        return toResponse(savedVendor, ownerEmail);
    }

    @Transactional(readOnly = true)
    public Page<VendorResponse> getVendors(Pageable pageable) {

        return vendorRepository
                .findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public VendorResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    /**
     * Entity lookup shared with {@code ProductService} when attaching a
     * product to its vendor.
     */
    @Transactional(readOnly = true)
    public Vendor getEntity(Long id) {

        return vendorRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vendor not found with id: " + id
                        ));
    }

    /**
     * Marks a vendor as verified, the administrative gate before a seller is
     * surfaced as trusted in the storefront.
     */
    @Transactional
    public VendorResponse verify(Long id, boolean verified) {

        Vendor vendor = getEntity(id);

        boolean previous =
                Boolean.TRUE.equals(vendor.getIsVerified());

        vendor.setIsVerified(verified);

        Vendor savedVendor =
                vendorRepository.save(vendor);

        Map<String, Object> details =
                new HashMap<>();

        details.put("vendorId", savedVendor.getId());
        details.put("businessName", savedVendor.getBusinessName());
        details.put("oldVerified", previous);
        details.put("newVerified", verified);

        auditService.log(
                "Vendor",
                "VENDOR_VERIFIED",
                details
        );

        return toResponse(savedVendor);
    }

    /**
     * Recomputes {@code vendors.rating_average} from every review across every
     * product the vendor owns.
     *
     * <p>Called by {@code ReviewService} after each new review, which is the
     * "automatically recalculate the average rating for both the product and
     * the vendor" requirement. The average is weighted by review volume rather
     * than being an average of per-product averages, so a product with fifty
     * reviews counts fifty times more than one with a single review.
     *
     * @return the recalculated average, or {@code 0.0} when the vendor has no
     *         reviews at all
     */
    @Transactional
    public double recalculateRatingAverage(Long vendorId) {

        Vendor vendor = getEntity(vendorId);

        Double average =
                reviewRepository
                        .findAverageRatingByVendorId(vendorId);

        double rounded =
                average == null
                        ? 0.0
                        : round(average);

        vendor.setRatingAverage(rounded);

        vendorRepository.save(vendor);

        return rounded;
    }

    /**
     * Promotes an existing account to ROLE_VENDOR and binds it to the vendor.
     *
     * @return the linked email, or {@code null} when no owner was requested
     */
    private String linkOwnerIfRequested(
            Vendor vendor,
            String requestedEmail) {

        String ownerEmail =
                normalise(requestedEmail);

        if (ownerEmail == null) {
            return null;
        }

        User owner =
                userRepository
                        .findByEmail(ownerEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No user found with email: "
                                                + ownerEmail
                                                + ". Register the account before "
                                                + "linking it to a vendor."
                                ));

        if (owner.getRole() == Role.ADMIN) {

            throw new IllegalArgumentException(
                    "User "
                            + ownerEmail
                            + " is an administrator and cannot be "
                            + "converted into a vendor account"
            );
        }

        if (owner.getVendor() != null
                && !owner.getVendor().getId().equals(vendor.getId())) {

            throw new IllegalArgumentException(
                    "User "
                            + ownerEmail
                            + " is already linked to vendor '"
                            + owner.getVendor().getBusinessName()
                            + "'"
            );
        }

        owner.setRole(Role.VENDOR);
        owner.setVendor(vendor);

        userRepository.save(owner);

        return ownerEmail;
    }

    private VendorResponse toResponse(Vendor vendor) {

        String ownerEmail =
                userRepository
                        .findFirstByVendorIdOrderByIdAsc(vendor.getId())
                        .map(User::getEmail)
                        .orElse(null);

        return toResponse(vendor, ownerEmail);
    }

    private VendorResponse toResponse(
            Vendor vendor,
            String ownerEmail) {

        return new VendorResponse(
                vendor.getId(),
                vendor.getBusinessName(),
                vendor.getSupportEmail(),
                vendor.getLogoUrl(),
                vendor.getRatingAverage(),
                vendor.getIsVerified(),
                vendor.getCreatedAt(),
                ownerEmail
        );
    }

    private String normalise(String value) {

        return Optional
                .ofNullable(value)
                .map(String::trim)
                .filter(trimmed -> !trimmed.isEmpty())
                .orElse(null);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
