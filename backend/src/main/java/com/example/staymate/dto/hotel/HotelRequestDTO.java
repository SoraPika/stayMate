package com.example.staymate.dto.hotel;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.staymate.dto.room.RoomRequestDTO;

public class HotelRequestDTO {
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private List<RoomRequestDTO> rooms; // List of rooms to create
    private MultipartFile image;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<RoomRequestDTO> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomRequestDTO> rooms) {
        this.rooms = rooms;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

}
