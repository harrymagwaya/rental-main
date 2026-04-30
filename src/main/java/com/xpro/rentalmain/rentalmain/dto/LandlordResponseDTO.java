import com.xpro.rentalmain.rentalmain.model.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record LandlordResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String profilePhoto,
        Gender gender,
        LocalDate dateOfBirth
) {}