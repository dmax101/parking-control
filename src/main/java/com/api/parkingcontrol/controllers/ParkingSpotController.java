package com.api.parkingcontrol.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

import org.springframework.web.bind.annotation.PutMapping;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {

        if (parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Car already registered");
        }

        if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking spot already registered");
        }

        if (parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Apartment and block already registered");
        }

        var parkingSpotModel = new ParkingSpotModel();

        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);

        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));

        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));

    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pegeable) {
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pegeable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if (!parkingSpotModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ParkingSpotMessage.NOT_FOUND.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if (!parkingSpotModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ParkingSpotMessage.NOT_FOUND);
        }

        parkingSpotService.delete(parkingSpotModelOptional.get());

        return ResponseEntity.status(HttpStatus.OK).body(ParkingSpotMessage.DELETED.getMessage());
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,
            @RequestBody @Valid ParkingSpotDto parkingSpotDto) {

        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if (!parkingSpotModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ParkingSpotMessage.NOT_FOUND.getMessage());
        }

        var parkingSpotModel = new ParkingSpotModel();

        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setId(id);
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    }
}

enum ParkingSpotMessage {
    CREATED("Parking spot created successfully"),
    UPDATED("Parking spot updated successfully"),
    DELETED("Parking spot deleted successfully"),
    NOT_FOUND("Parking spot not found"),
    ALREADY_EXISTS("Parking spot already exists"),
    NOT_AVAILABLE("Parking spot not available"),
    AVAILABLE("Parking spot available");

    private String message;

    ParkingSpotMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}