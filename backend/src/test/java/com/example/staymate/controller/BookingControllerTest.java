package com.example.staymate.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.staymate.dto.booking.BookingRequestDTO;
import com.example.staymate.entity.booking.Booking;
import com.example.staymate.entity.enums.BookingStatus;
import com.example.staymate.entity.enums.RoomStatus;
import com.example.staymate.entity.enums.RoomType;
import com.example.staymate.entity.hotel.Hotel;
import com.example.staymate.entity.room.Room;
import com.example.staymate.entity.room.RoomId;
import com.example.staymate.entity.user.User;
import com.example.staymate.factory.RoomFactory;
import com.example.staymate.service.BookingService;
import com.example.staymate.service.RoomService;
import com.example.staymate.service.UserService;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @Mock
    private RoomService roomService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    }

    @Test
    void testCreateBooking_Success() throws Exception {
        Long userId = 1L;
        Long hotelId = 1L;
        Long generatedRoomId = 101L;
        LocalDate checkInDate = LocalDate.of(2025, 2, 23); // Set the check-in date as per the request
        LocalDate checkOutDate = LocalDate.of(2025, 2, 25); // Set the check-out date as per the request
        double totalAmount = 100.0;
        RoomType roomType = RoomType.SINGLE;
        double pricePerNight = 50.0;
        int maxOccupancy = 2;

        // Creating mock data for User and Room
        User user = new User();
        user.setId(userId);
        when(userService.getUserById(userId)).thenReturn(user);

        lenient().when(roomService.isRoomAvailable(eq(hotelId), eq(generatedRoomId), eq(checkInDate), eq(checkOutDate))).thenReturn(true);
        
        // Simulate hotel creation
        Hotel mockHotel = new Hotel();
        mockHotel.setId(hotelId);

        // Creating BookingRequestDTO mock
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.setUserId(userId);
        bookingRequestDTO.setHotelId(hotelId);
        bookingRequestDTO.setRoomId(generatedRoomId);
        bookingRequestDTO.setCheckInDate(checkInDate);
        bookingRequestDTO.setCheckOutDate(checkOutDate);
        bookingRequestDTO.setTotalAmount(totalAmount);

        // Simulate room creation using RoomFactory
        Room mockRoom = RoomFactory.createRoom(mockHotel, generatedRoomId, roomType, pricePerNight, maxOccupancy);
        mockRoom.setId(new RoomId(hotelId, generatedRoomId));
        mockRoom.setMaxOccupancy(maxOccupancy);
        mockRoom.setPricePerNight(pricePerNight);
        mockRoom.setStatus(RoomStatus.AVAILABLE);

        // Mock RoomService to return the created room from the factory
        lenient().when(roomService.createRoom(eq(mockHotel), eq(generatedRoomId), eq(roomType), eq(pricePerNight), eq(maxOccupancy)))
            .thenReturn(mockRoom);

        when(roomService.bookRoom(eq(hotelId), eq(generatedRoomId), eq(checkInDate), eq(checkOutDate))).thenReturn(mockRoom);

        // Creating a Booking mock
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(mockRoom);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setBookingDate(checkInDate);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingService.createBooking(any(Booking.class))).thenReturn(booking);

        // Perform the POST request to create a booking
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"hotelId\":1,\"roomId\":101,\"checkInDate\":\"2025-02-23\",\"checkOutDate\":\"2025-02-25\",\"totalAmount\":100.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Booking created successfully"))
                .andExpect(jsonPath("$.data.bookingId").value(1));
    }

    @Test
    void testCreateBooking_RoomUnavailable() throws Exception {
        Long userId = 1L;
        Long hotelId = 1L;
        Long roomId = 101L;
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = checkInDate.plusDays(2);

        User user = new User();
        user.setId(userId);
        when(userService.getUserById(userId)).thenReturn(user);

        // Mock data for booking request
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.setUserId(userId);
        bookingRequestDTO.setHotelId(hotelId);
        bookingRequestDTO.setRoomId(roomId);
        bookingRequestDTO.setCheckInDate(checkInDate);
        bookingRequestDTO.setCheckOutDate(checkOutDate);
        bookingRequestDTO.setTotalAmount(100.0);

        // Mock the services with lenient stubbing
        lenient().when(roomService.isRoomAvailable(eq(hotelId), eq(roomId), eq(checkInDate), eq(checkOutDate))).thenReturn(false);

        // Perform the POST request
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"hotelId\":1,\"roomId\":101,\"checkInDate\":\"2025-02-25\",\"checkOutDate\":\"2025-02-27\",\"totalAmount\":100.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Room is not available for the selected dates"));

        // Verifying that createBooking was not called due to room unavailability
        verify(bookingService, times(0)).createBooking(any(Booking.class));
    }

    @Test
    void testCreateBooking_InvalidDates() throws Exception {
        Long userId = 1L;
        Long hotelId = 1L;
        Long roomId = 101L;
        LocalDate checkInDate = LocalDate.now().plusDays(3);
        LocalDate checkOutDate = checkInDate.minusDays(1);  // Invalid check-out date

        // BookingRequestDTO mock
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.setUserId(userId);
        bookingRequestDTO.setHotelId(hotelId);
        bookingRequestDTO.setRoomId(roomId);
        bookingRequestDTO.setCheckInDate(checkInDate);
        bookingRequestDTO.setCheckOutDate(checkOutDate);
        bookingRequestDTO.setTotalAmount(100.0);

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"hotelId\":1,\"roomId\":101,\"checkInDate\":\"2025-02-25\",\"checkOutDate\":\"2025-02-24\",\"totalAmount\":100.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Check-out date must be after check-in date"));
    }

    @Test
    void testCreateBooking_BookingRoomFailure() throws Exception {
        Long userId = 1L;
        Long hotelId = 1L;
        Long roomId = 101L;
        LocalDate checkInDate = LocalDate.now().plusDays(10);
        LocalDate checkOutDate = checkInDate.plusDays(1);

        User mockUser = new User();
        mockUser.setId(userId);
        when(userService.getUserById(userId)).thenReturn(mockUser);

        lenient().when(roomService.isRoomAvailable(hotelId, roomId, checkInDate, checkOutDate)).thenReturn(true);  // Simulate room availability
        lenient().when(roomService.bookRoom(hotelId, roomId, checkInDate, checkOutDate)).thenThrow(new RuntimeException("Room booking failed. The room might not be available."));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"hotelId\":1,\"roomId\":101,\"checkInDate\":\"2025-02-25\",\"checkOutDate\":\"2025-02-26\",\"totalAmount\":100.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Room is not available for the selected dates"));  // Expect the error message    }

    }

    @Test
    void testGetBookingById_BookingFound() throws Exception {
        
        Long bookingId = 1L;
        Booking mockBooking = new Booking();
        mockBooking.setId(bookingId);
        mockBooking.setStatus(BookingStatus.PENDING);
        when(bookingService.getBookingById(bookingId)).thenReturn(mockBooking);

        mockMvc.perform(get("/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingId").value(bookingId))
                .andExpect(jsonPath("$.data.status").value(BookingStatus.PENDING.toString()));
    }

    @Test
    void testGetBookingById_BookingNotFound() throws Exception {
        // Arrange
        Long bookingId = 1L;
        when(bookingService.getBookingById(bookingId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/bookings/{id}", bookingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found"));

    }

    @Test
    void testCancelBooking_BookingFound() throws Exception {
        // Arrange
        Long bookingId = 1L;
        Booking mockBooking = new Booking();
        mockBooking.setId(bookingId);
        mockBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingService.cancelBooking(bookingId)).thenReturn(mockBooking);

        // Act & Assert
        mockMvc.perform(delete("/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Booking cancelled successfully"))
                .andExpect(jsonPath("$.data.bookingId").value(bookingId))
                .andExpect(jsonPath("$.data.status").value(BookingStatus.CANCELLED.toString()));
    }

    @Test
    void testCancelBooking_BookingNotFound() throws Exception {

        Long bookingId = 1L;
        when(bookingService.cancelBooking(bookingId)).thenReturn(null);

        mockMvc.perform(delete("/bookings/{id}", bookingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found"));
    }

    @Test
    void testGetBookingsForHotel_BookingsFound() throws Exception {

        Long hotelId = 1L;
        Long bookingId = 1L;
        BookingStatus bookingStatus= BookingStatus.CONFIRMED;

        Booking mockBooking = new Booking();
        mockBooking.setId(bookingId);
        mockBooking.setStatus(bookingStatus);

        List<Booking> mockBookings = new ArrayList<>();
        mockBookings.add(mockBooking);
        when(bookingService.getBookingsByHotel(hotelId)).thenReturn(mockBookings);

        mockMvc.perform(get("/bookings/hotel/{hotelId}", hotelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").exists());
    }

    @Test
    void testGetBookingsForHotel_NoBookings() throws Exception {

        Long hotelId = 1L;
        when(bookingService.getBookingsByHotel(hotelId)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/bookings/hotel/{hotelId}", hotelId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetBookingsForUser_BookingsFound() throws Exception {

        Long userId = 1L;
        Long bookingId = 1L;
        BookingStatus bookingStatus= BookingStatus.CONFIRMED;

        Booking mockBooking = new Booking();
        mockBooking.setId(bookingId);
        mockBooking.setStatus(bookingStatus);

        List<Booking> mockBookings = new ArrayList<>();
        mockBookings.add(mockBooking);
        when(bookingService.getBookingsByUser(userId)).thenReturn(mockBookings);

        mockMvc.perform(get("/bookings/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").exists());
    }

    @Test
    void testGetBookingsForUser_NoBookings() throws Exception {
        Long userId = 1L;
        when(bookingService.getBookingsByUser(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/bookings/user/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").isEmpty());
    }

}
