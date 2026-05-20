package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.*;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Restaurant;
import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.exception.BadRequestException;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.RestaurantRepository;
import com.app.tastefrancesinhasbackend.repository.ReviewRepository;
import com.app.tastefrancesinhasbackend.security.CurrentUserContext;
import com.app.tastefrancesinhasbackend.spec.FrancesinhaSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FrancesinhaService {

    private final FrancesinhaRepository francesinhaRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final CurrentUserContext currentUser;

    // Devuelve las francesinhas aprobadas. Los filtros son opcionales: sin ninguno hya filtros, devuelve todas.
    @Transactional(readOnly = true)
    public Page<FrancesinhaResponse> findAllAccepted(String name, String city, FrancesinhaType type,
                                                     Long restaurantId, Pageable pageable) {
        Specification<Francesinha> spec = FrancesinhaSpec.withFilters(FrancesinhaStatus.ACCEPTED, name, type, city, restaurantId);
        Page<Francesinha> page = francesinhaRepository.findAll(spec, pageable);
        Map<Long, String> covers = fetchCoverPhotos(page.getContent().stream().map(Francesinha::getId).toList());
        return page.map(f -> FrancesinhaDTO.responsePublic(f, covers.get(f.getId()), null));
    }

    // Busca una francesinha aprobada por id. Si no existe o no está aprobada, lanza 404.
    // En el detalle devolvemos TODAS las URLs de fotos de las reviews para alimentar el carrusel,
    // independientemente de la paginacion del endpoint de reviews.
    @Transactional(readOnly = true)
    public FrancesinhaResponse findById(Long id) {
        Francesinha francesinha = francesinhaRepository.findByIdAndStatus(id, FrancesinhaStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + id));
        String coverPhotoUrl = fetchCoverPhotos(List.of(id)).get(id);
        List<String> photoUrls = reviewRepository.findPhotoUrlsByFrancesinhaId(id);
        return FrancesinhaDTO.responsePublic(francesinha, coverPhotoUrl, photoUrls);
    }

    // Contadores agregados para el dashboard de admin: pendientes, aprobadas, rechazadas y total.
    // El total se calcula sumando para evitar una query extra al COUNT(*) de la tabla entera.
    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        long pending = francesinhaRepository.countByStatus(FrancesinhaStatus.PENDING);
        long accepted = francesinhaRepository.countByStatus(FrancesinhaStatus.ACCEPTED);
        long rejected = francesinhaRepository.countByStatus(FrancesinhaStatus.REJECTED);
        return new StatsResponse(pending, accepted, rejected, pending + accepted + rejected);
    }

    // Listado para el admin filtrado por estado (ACCEPTED o REJECTED). A diferencia del
    // listado publico devolvemos responsePrivate para que el admin vea quien la propuso.
    @Transactional(readOnly = true)
    public Page<FrancesinhaResponse> findAllForAdmin(FrancesinhaStatus status, Pageable pageable) {
        Page<Francesinha> page = francesinhaRepository.findAll(
                FrancesinhaSpec.withFilters(status, null, null, null, null), pageable);
        Map<Long, String> covers = fetchCoverPhotos(page.getContent().stream().map(Francesinha::getId).toList());
        return page.map(f -> FrancesinhaDTO.responsePrivate(f, covers.get(f.getId()), null));
    }

    // Lista las francesinhas que el admin todavía no ha revisado.
    @Transactional(readOnly = true)
    public Page<FrancesinhaResponse> findAllPending(Pageable pageable) {
        Page<Francesinha> page = francesinhaRepository.findAll(
                FrancesinhaSpec.withFilters(FrancesinhaStatus.PENDING, null, null, null, null), pageable);
        Map<Long, String> covers = fetchCoverPhotos(page.getContent().stream().map(Francesinha::getId).toList());
        return page.map(f -> FrancesinhaDTO.responsePrivate(f, covers.get(f.getId()), null));
    }

    // Igual que findById pero sin filtrar por estado, para que el admin pueda ver cualquier francesinha.
    // Devuelve la francesinha junto a la review de la propuesat: se envia todo la info en ela misma dto
    @Transactional(readOnly = true)
    public PendingFrancesinhaWithReviewResponse findByIdForAdmin(Long id) {
        Francesinha francesinha = francesinhaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + id));
        String coverPhotoUrl = fetchCoverPhotos(List.of(id)).get(id);
        List<String> photoUrls = reviewRepository.findPhotoUrlsByFrancesinhaId(id);
        Review proposalReview = reviewRepository.findFirstByFrancesinhaIdOrderByCreatedAtAsc(id).orElse(null);
        return FrancesinhaDTO.pendingWithReview(francesinha, coverPhotoUrl, photoUrls, proposalReview);
    }

    // El admin aprueba o rechaza una francesinha pendiente. No se puede volver a poner en PENDING.
    @Transactional
    public FrancesinhaResponse updateStatus(Long id, FrancesinhaStatusRequest request) {
        if (request.status() == FrancesinhaStatus.PENDING) {
            throw new BadRequestException("El estado no puede ser PENDING");
        }

        Francesinha francesinha = francesinhaRepository.findByIdAndStatus(id, FrancesinhaStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha pendiente no encontrada: " + id));

        francesinha.setStatus(request.status());

        // Si aprobamos una francesinha cuyo restaurante todavia no esta activo
        if (request.status() == FrancesinhaStatus.ACCEPTED) {
            Restaurant restaurant = francesinha.getRestaurant();
            if (Boolean.FALSE.equals(restaurant.getActive())) {
                restaurant.setActive(true);
                restaurantRepository.save(restaurant);
            }
        }

        Francesinha saved = francesinhaRepository.save(francesinha);
        String coverPhotoUrl = fetchCoverPhotos(List.of(saved.getId())).get(saved.getId());
        return FrancesinhaDTO.responsePrivate(saved, coverPhotoUrl, null);
    }

    // Un usuario propone una nueva francesinha. Queda en PENDING hasta que un admin la revise.
    @Transactional
    public FrancesinhaResponse propose(FrancesinhaRequest request) {
        User proposedBy = currentUser.getUser();

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado: " + request.restaurantId()));

        Francesinha francesinha = Francesinha.builder()
                .restaurant(restaurant)
                .proposedBy(proposedBy)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .hasEgg(request.hasEgg())
                .hasFries(request.hasFries())
                .isSpicy(request.isSpicy())
                .type(request.type())
                .build();

        return FrancesinhaDTO.responsePublic(francesinhaRepository.save(francesinha), null, null);
    }

    // Una sola query nativa con ROW_NUMBER() saca la foto mas reciente por francesinha.
    // Mapeamos el List<Object[]> a Map<francesinhaId, photoUrl>.
    private Map<Long, String> fetchCoverPhotos(Collection<Long> francesinhaIds) {
        if (francesinhaIds.isEmpty()) {
            return Map.of();
        }
        return reviewRepository.findCoverPhotoUrlsByFrancesinhaIds(francesinhaIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> (String) row[1]
                ));
    }
}
