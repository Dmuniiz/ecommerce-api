package com.api.e_commerce.address;

import com.api.e_commerce.address.dto.CreateAddressRequest;
import com.api.e_commerce.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    private String country;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    @ElementCollection(targetClass = AddressType.class)
    @CollectionTable(
            name = "address_type",
            joinColumns = @JoinColumn(name = "address_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Set<AddressType> addressType;


    public Address(CreateAddressRequest data, User user) {
        this.user = user;
        this.street = data.street();
        this.number = data.number();
        this.complement = data.complement();
        this.neighborhood = data.neighborhood();
        this.city = data.city();
        this.state = data.state();
        this.zipCode = data.zipCode();
        this.country = data.country();
        this.isDefault = data.isDefault();
        this.addressType = data.addressType();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void updateFields(Set<AddressType> addressTypes, String complement, String number) {
        this.addressType.addAll(addressTypes);
        this.complement = complement;
        this.number = number;
    }
}
