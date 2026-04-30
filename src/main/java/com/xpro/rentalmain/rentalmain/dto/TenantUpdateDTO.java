import com.xpro.rentalmain.rentalmain.entity.Tenant;

public record TenantUpdateDTO(
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String nationalId,
        Tenant.TenantStatus status
) {}