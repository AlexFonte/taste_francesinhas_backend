package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.ReviewDTO;
import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewRequest;
import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewResponse;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.exception.BadRequestException;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.ReviewRepository;
import com.app.tastefrancesinhasbackend.security.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Set<String> ALLOWED_PHOTO_MIME = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_PHOTO_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Map<String, String> EXTENSION_TO_MIME = Map.of(
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "png",  "image/png",
            "webp", "image/webp"
    );

    private final ReviewRepository reviewRepository;
    private final FrancesinhaRepository francesinhaRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final CurrentUserContext currentUser;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> findByFrancesinha(Long francesinhaId, Pageable pageable) {
        francesinhaRepository.findByIdAndStatus(francesinhaId, FrancesinhaStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        return reviewRepository.findByFrancesinhaId(francesinhaId, pageable)
                .map(ReviewDTO::responsePublic);
    }

    // Igual que findByFrancesinha pero sin filtrar por estado de la francesinha,
    // pensado para que el admin pueda ver la review que dejo el proponente al crear
    // la propuesta (la francesinha esta en PENDING).
    @Transactional(readOnly = true)
    public Page<ReviewResponse> findByFrancesinhaForAdmin(Long francesinhaId, Pageable pageable) {
        francesinhaRepository.findById(francesinhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        return reviewRepository.findByFrancesinhaId(francesinhaId, pageable)
                .map(ReviewDTO::responsePublic);
    }

    // POST /francesinhas/{id}/reviews - autenticado, crea una review con foto opcional.
    // Si la subida a Supabase falla, @Transactional revierte el save de la review -> nada queda en BD.
    @Transactional
    public ReviewResponse create(Long francesinhaId, ReviewRequest request, MultipartFile file) {
        // Validamos la foto ANTES de tocar BD para fail-fast (sin crear review si la foto no es valida).
        if (file != null && !file.isEmpty() && !isValidPhoto(file)) {
            throw new BadRequestException("Tipo de archivo no soportado. Solo JPG, JPEG, PNG o WebP.");
        }

        // Si la review viene del flujo de proponer, la francesinha esta PENDING; si no, ACCEPTED.
        FrancesinhaStatus searchByStatus = Boolean.TRUE.equals(request.propuesta())
                ? FrancesinhaStatus.PENDING
                : FrancesinhaStatus.ACCEPTED;

        Francesinha francesinha = francesinhaRepository.findByIdAndStatus(francesinhaId, searchByStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        BigDecimal reviewAvg = BigDecimal.valueOf(
                (request.scoreFlavor() + request.scoreSauce() + request.scoreBread() + request.scorePresentation()) / 4.0
        ).setScale(2, RoundingMode.HALF_UP);

        Review review = Review.builder()
                .francesinha(francesinha)
                .user(currentUser.getUser())
                .scoreFlavor(request.scoreFlavor())
                .scoreSauce(request.scoreSauce())
                .scoreBread(request.scoreBread())
                .scorePresentation(request.scorePresentation())
                .avgScore(reviewAvg)
                .comment(request.comment())
                .build();

        reviewRepository.save(review);

        // Si hay foto, la subimos despues del save para tener el id en el nombre del fichero.
        // Si el upload falla, la transaccion revierte el save y devolvemos error.
        if (file != null && !file.isEmpty()) {
            String extension = getExtension(file.getOriginalFilename());
            String fileName = "%d_%s.%s".formatted(review.getId(), UUID.randomUUID(), extension);
            String contentType = EXTENSION_TO_MIME.getOrDefault(extension, "image/jpeg");
            String publicUrl = supabaseStorageService.upload(file, fileName, contentType);
            review.setPhotoUrl(publicUrl);
        }

        // La BD suma todos los avgScore de las reviews (incluida la nueva) y recalcula la media
        francesinhaRepository.updateScore(francesinha.getId());

        return ReviewDTO.responsePublic(review);
    }

    // Saca la extension del nombre original del fichero ("foto.JPG" -> "jpg").
    // Si no tiene punto o es null, asumimos jpg para no dejar el path sin extension.
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    // Foto valida si el MIME esta en la lista, o si la extension del fichero esta en la lista.
    // Doble check porque distintos clientes (Bruno, curl, frontend) envian el MIME de forma inconsistente.
    private boolean isValidPhoto(MultipartFile file) {
        if (ALLOWED_PHOTO_MIME.contains(file.getContentType())) {
            return true;
        }
        return ALLOWED_PHOTO_EXTENSIONS.contains(getExtension(file.getOriginalFilename()));
    }
}