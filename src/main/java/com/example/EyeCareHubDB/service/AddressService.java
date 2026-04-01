package com.example.EyeCareHubDB.service;

import com.example.EyeCareHubDB.dto.AddressDTO;
import com.example.EyeCareHubDB.dto.AddressCreateRequest;
import com.example.EyeCareHubDB.dto.AddressUpdateRequest;
import com.example.EyeCareHubDB.entity.Address;
import com.example.EyeCareHubDB.entity.Customer;
import com.example.EyeCareHubDB.repository.AddressRepository;
import com.example.EyeCareHubDB.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// ============================================================
// SERVICE: AddressService — Quản lý địa chỉ giao hàng/thanh toán của khách hàng.
// Mỗi customer có nhiều địa chỉ, chỉ 1 địa chỉ mặc định (isDefaultShip=true) tại 1 thời điểm.
// Địa chỉ mặc định tự được áp vào checkout khi khách không chọn thủ công.
// ============================================================
@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {
    
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    
    public List<AddressDTO> getAddressesByCustomerId(Long customerId) {
        return addressRepository.findByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public AddressDTO getAddressById(Long id) {
        return addressRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
    }
    
    // Lấy địa chỉ MẶC ĐỊNH (isDefaultShip=true). Dùng khi checkout để tự điền địa chỉ giao hàng.
    public AddressDTO getDefaultAddress(Long customerId) {
        return addressRepository.findDefaultAddressByCustomerId(customerId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("No default address found for customer: " + customerId));
    }
    
    // Tạo địa chỉ mới. Country mặc định = "Vietnam".
    public AddressDTO createAddress(Long customerId, AddressCreateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        Address address = Address.builder()
                .customer(customer)
                .recipientName(request.getRecipientName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .province(request.getProvince())
                .country("Vietnam")
                .isDefaultShip(request.getIsDefaultShip() != null ? request.getIsDefaultShip() : false)
                .isDefaultBill(request.getIsDefaultBill() != null ? request.getIsDefaultBill() : false)
                .build();
        
        Address saved = addressRepository.save(address);
        return toDTO(saved);
    }
    
    public AddressDTO updateAddress(Long id, AddressUpdateRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
        
        if (request.getRecipientName() != null) {
            address.setRecipientName(request.getRecipientName());
        }
        if (request.getPhoneNumber() != null) {
            address.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddressLine1() != null) {
            address.setAddressLine1(request.getAddressLine1());
        }
        if (request.getProvince() != null) {
            address.setProvince(request.getProvince());
        }
        if (request.getIsDefaultShip() != null) {
            address.setIsDefaultShip(request.getIsDefaultShip());
        }
        if (request.getIsDefaultBill() != null) {
            address.setIsDefaultBill(request.getIsDefaultBill());
        }
        
        Address updated = addressRepository.save(address);
        return toDTO(updated);
    }
    
    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new RuntimeException("Address not found with id: " + id);
        }
        addressRepository.deleteById(id);
    }
    
    // Đặt địa chỉ mặc định: bỏ isDefaultShip của địa chỉ cũ, gán true cho địa chỉ mới.
    // Đảm bảo mỗi customer chỉ có 1 địa chỉ mặc định tại 1 thời điểm.
    public void setDefaultAddress(Long customerId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));
        
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to this customer");
        }
        
        addressRepository.findDefaultAddressByCustomerId(customerId).ifPresent(defaultAddr -> {
            defaultAddr.setIsDefaultShip(false);
            addressRepository.save(defaultAddr);
        });
        
        address.setIsDefaultShip(true);
        addressRepository.save(address);
    }
    
    private AddressDTO toDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .ward(address.getWard())
                .district(address.getDistrict())
                .province(address.getProvince())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefaultShip(address.getIsDefaultShip())
                .isDefaultBill(address.getIsDefaultBill())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
