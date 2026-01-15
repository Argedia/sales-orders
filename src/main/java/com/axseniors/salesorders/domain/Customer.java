package com.axseniors.salesorders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer {

@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private UUID id;

@NotBlank
@Column(nullable = false)
private String name;

@NotBlank
@Column(nullable = false)
private String contactName;

@Email
@Column(nullable = false)
private String email;

@NotBlank
@Pattern(regexp = "[0-9+\\-() ]{7,20}", message = "Teléfono inválido")
@Column(nullable = false)
private String phone;

@Column
private String address;

@Column
private String city;

@Column(unique = true)
private String taxId;

    public Customer(String name, String contactName, String email, String phone, String address, String city, String taxId) {
        this.name = name;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.taxId = taxId;
    }
}
